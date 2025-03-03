package ma.hmzelidrissi.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hmzelidrissi.paymentservice.domain.Payment;
import ma.hmzelidrissi.paymentservice.domain.PaymentMethod;
import ma.hmzelidrissi.paymentservice.domain.PaymentStatus;
import ma.hmzelidrissi.paymentservice.dto.PaymentRequest;
import ma.hmzelidrissi.paymentservice.repository.PaymentRepository;
import ma.hmzelidrissi.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        log.info("Webhook Stripe reçu");

        Event event;

        try {
            // Vérifier la signature si le secret est configuré
            if (webhookSecret != null && !webhookSecret.isEmpty() && sigHeader != null) {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } else {
                // Pour le développement, traiter l'événement sans vérification
                log.warn("Traitement de l'événement sans vérification de signature (mode développement)");
                event = objectMapper.readValue(payload, Event.class);
            }
        } catch (SignatureVerificationException e) {
            log.warn("Signature de webhook invalide: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Signature invalide");
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse du payload: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Payload invalide");
        }

        // Traitement des événements Stripe
        try {
            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                case "charge.succeeded":
                    handleChargeSucceeded(event);
                    break;
                case "charge.failed":
                    handleChargeFailed(event);
                    break;
                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;
                default:
                    log.info("Type d'événement Stripe non géré: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement: {}", e.getMessage());
            return ResponseEntity.status(500).body("Erreur de traitement: " + e.getMessage());
        }

        return ResponseEntity.ok("Webhook traité avec succès");
    }

    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> stripeObjectOptional = dataObjectDeserializer.getObject();

        if (stripeObjectOptional.isPresent()) {
            Session session = (Session) stripeObjectOptional.get();
            log.info("Session de checkout complétée: {}", session.getId());

            // Récupérer les informations du paiement à partir des métadonnées
            String commandeReference = session.getMetadata().get("commandeReference");

            if (commandeReference != null) {
                // Créer un nouvel objet PaymentRequest avec les informations de la session
                PaymentRequest paymentRequest = new PaymentRequest(
                        commandeReference,
                        BigDecimal.valueOf(session.getAmountTotal() / 100.0), // Convertir de centimes
                        PaymentMethod.CARTE_BANCAIRE,
                        null, // Pas de détails de carte car traité par Stripe
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                // Traiter le paiement et l'enregistrer dans la base de données
                paymentService.processPayment(paymentRequest);
            } else {
                log.warn("Référence de commande manquante dans les métadonnées de la session");
            }
        } else {
            log.warn("Impossible de désérialiser l'objet Session");
        }
    }

    private void handleChargeSucceeded(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            com.stripe.model.Charge charge = (com.stripe.model.Charge) dataObjectDeserializer.getObject().get();
            String chargeId = charge.getId();
            log.info("Charge réussie: {}", chargeId);

            // Trouver le paiement associé à cette charge Stripe
            Optional<Payment> paymentOpt = findPaymentByStripeChargeId(chargeId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.COMPLETED);
                log.info("Paiement (ID: {}) mis à jour avec statut COMPLETED", payment.getId());
            }
        }
    }

    private void handleChargeFailed(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            com.stripe.model.Charge charge = (com.stripe.model.Charge) dataObjectDeserializer.getObject().get();
            String chargeId = charge.getId();
            log.info("Charge échouée: {}", chargeId);

            // Trouver le paiement associé à cette charge Stripe
            Optional<Payment> paymentOpt = findPaymentByStripeChargeId(chargeId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.FAILED);
                log.info("Paiement (ID: {}) mis à jour avec statut FAILED", payment.getId());
            }
        }
    }

    private void handleChargeRefunded(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            com.stripe.model.Charge charge = (com.stripe.model.Charge) dataObjectDeserializer.getObject().get();
            String chargeId = charge.getId();
            boolean fullyRefunded = charge.getRefunded();

            log.info("Charge remboursée: {}, remboursement complet: {}", chargeId, fullyRefunded);

            // Trouver le paiement associé à cette charge Stripe
            Optional<Payment> paymentOpt = findPaymentByStripeChargeId(chargeId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (fullyRefunded) {
                    paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.REFUNDED);
                    log.info("Paiement (ID: {}) mis à jour avec statut REFUNDED", payment.getId());
                } else {
                    paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.PARTIALLY_REFUNDED);
                    log.info("Paiement (ID: {}) mis à jour avec statut PARTIALLY_REFUNDED", payment.getId());
                }
            }
        }
    }

    private Optional<Payment> findPaymentByStripeChargeId(String chargeId) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getTransactions().stream()
                        .anyMatch(transaction -> chargeId.equals(transaction.getStripeChargeId())))
                .findFirst();
    }
}