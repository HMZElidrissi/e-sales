package ma.hmzelidrissi.paymentservice.controller;

import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hmzelidrissi.paymentservice.domain.PaymentStatus;
import ma.hmzelidrissi.paymentservice.dto.*;
import ma.hmzelidrissi.paymentservice.exception.PaymentException;
import ma.hmzelidrissi.paymentservice.service.CheckoutService;
import ma.hmzelidrissi.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final CheckoutService checkoutService;

    /**
     * Point d'entrée principal pour créer une session de paiement Stripe Checkout
     * Cette méthode crée une session Checkout mais n'effectue pas le paiement directement
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> createCheckoutSession(@Valid @RequestBody CheckoutRequest checkoutRequest) {
        try {
            log.info("Création d'une session checkout pour la commande: {}", checkoutRequest.commandeReference());
            CheckoutResponse response = checkoutService.createCheckoutSession(checkoutRequest);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (StripeException e) {
            log.error("Erreur lors de la création de la session checkout: {}", e.getMessage());
            throw new PaymentException("Échec de la création de la session: " + e.getMessage());
        }
    }

    /**
     * Traiter un paiement après la confirmation de Stripe
     * Cette méthode est généralement appelée par le webhook Stripe quand le paiement est confirmé
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        log.info("Traitement du paiement pour la commande: {}", paymentRequest.commandeReference());
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtenir un paiement par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.info("Récupération du paiement avec ID: {}", id);
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir tous les paiements pour une commande
     */
    @GetMapping("/commande/{reference}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCommandeReference(@PathVariable String reference) {
        log.info("Récupération des paiements pour la commande: {}", reference);
        List<PaymentResponse> response = paymentService.getPaymentsByCommandeReference(reference);
        return ResponseEntity.ok(response);
    }

    /**
     * Traiter un remboursement
     * Cette méthode met simplement à jour le statut dans la base de données
     */
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> processRefund(@Valid @RequestBody RefundRequest refundRequest) {
        log.info("Demande de remboursement reçue pour le paiement ID: {}", refundRequest.paymentId());
        RefundResponse response = paymentService.processRefund(refundRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtenir tous les remboursements pour un paiement
     */
    @GetMapping("/{id}/refunds")
    public ResponseEntity<List<RefundResponse>> getRefundsByPaymentId(@PathVariable Long id) {
        log.info("Récupération des remboursements pour le paiement ID: {}", id);
        List<RefundResponse> response = paymentService.getRefundsByPaymentId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre à jour le statut d'un paiement
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<Void> updatePaymentStatus(@PathVariable Long id, @PathVariable String status) {
        log.info("Mise à jour du statut du paiement ID: {} vers: {}", id, status);
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            paymentService.updatePaymentStatus(id, paymentStatus);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Statut de paiement invalide: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }
}