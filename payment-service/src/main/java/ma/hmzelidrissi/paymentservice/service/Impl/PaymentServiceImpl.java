package ma.hmzelidrissi.paymentservice.service.Impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hmzelidrissi.paymentservice.domain.*;
import ma.hmzelidrissi.paymentservice.dto.PaymentRequest;
import ma.hmzelidrissi.paymentservice.dto.PaymentResponse;
import ma.hmzelidrissi.paymentservice.dto.RefundRequest;
import ma.hmzelidrissi.paymentservice.dto.RefundResponse;
import ma.hmzelidrissi.paymentservice.exception.PaymentException;
import ma.hmzelidrissi.paymentservice.repository.PaymentRepository;
import ma.hmzelidrissi.paymentservice.repository.RefundRepository;
import ma.hmzelidrissi.paymentservice.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        log.info("Traitement du paiement pour la commande: {}", paymentRequest.commandeReference());

        // Création de l'entité de paiement
        Payment payment = new Payment();
        payment.setAmount(paymentRequest.amount());
        payment.setCommandeReference(paymentRequest.commandeReference());
        payment.setPaymentMethod(paymentRequest.paymentMethod());
        payment.setStatus(PaymentStatus.PENDING);

        // Créer une transaction
        Transaction transaction = new Transaction();
        transaction.setReference(generateTransactionReference());
        transaction.setStatus(PaymentStatus.PENDING);

        payment.addTransaction(transaction);

        try {
            // Les données de paiement sont maintenant traitées via Stripe Checkout
            // Nous ne manipulons plus directement les données de carte

            // Simuler le statut de paiement en fonction de la méthode choisie
            switch (payment.getPaymentMethod()) {
                case CARTE_BANCAIRE:
                    // Pour les cartes, le statut est mis à jour via les webhooks Stripe
                    // On garde le statut PENDING jusqu'à la confirmation par webhook
                    transaction.setDetails("En attente de confirmation via Stripe Checkout");
                    break;
                case PAYPAL:
                    // Simuler un paiement PayPal réussi
                    transaction.setStatus(PaymentStatus.COMPLETED);
                    payment.setStatus(PaymentStatus.COMPLETED);
                    transaction.setDetails("Paiement PayPal: " + paymentRequest.paypalEmail());
                    break;
                case VIREMENT:
                    // Pour un virement, le statut reste en attente jusqu'à confirmation manuelle
                    transaction.setDetails("En attente de virement: " + paymentRequest.accountNumber());
                    break;
                default:
                    transaction.setDetails("Méthode de paiement non reconnue");
            }

            payment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .id(payment.getId())
                    .commandeReference(payment.getCommandeReference())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .status(payment.getStatus())
                    .createdAt(payment.getCreatedAt())
                    .transactionReference(transaction.getReference())
                    .build();

        } catch (Exception e) {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setDetails("Erreur: " + e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("Erreur lors du traitement du paiement: {}", e.getMessage());
            throw new PaymentException("Échec du paiement: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Paiement non trouvé avec l'ID: " + id));

        String transactionReference = null;
        if (!payment.getTransactions().isEmpty()) {
            transactionReference = payment.getTransactions().get(0).getReference();
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .commandeReference(payment.getCommandeReference())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .transactionReference(transactionReference)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCommandeReference(String commandeReference) {
        List<Payment> payments = paymentRepository.findByCommandeReference(commandeReference);

        return payments.stream().map(payment -> {
            String transactionReference = null;
            if (!payment.getTransactions().isEmpty()) {
                transactionReference = payment.getTransactions().get(0).getReference();
            }

            return PaymentResponse.builder()
                    .id(payment.getId())
                    .commandeReference(payment.getCommandeReference())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .status(payment.getStatus())
                    .createdAt(payment.getCreatedAt())
                    .transactionReference(transactionReference)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RefundResponse processRefund(RefundRequest refundRequest) {
        Payment payment = paymentRepository.findById(refundRequest.paymentId())
                .orElseThrow(() -> new PaymentException("Paiement non trouvé avec l'ID: " + refundRequest.paymentId()));

        // Vérifier si le paiement est déjà remboursé
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentException("Ce paiement a déjà été entièrement remboursé");
        }

        // Vérifier si le montant du remboursement est valide
        BigDecimal totalRefundedAmount = payment.getRefunds().stream()
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingAmount = payment.getAmount().subtract(totalRefundedAmount);

        if (refundRequest.amount().compareTo(remainingAmount) > 0) {
            throw new PaymentException("Le montant du remboursement dépasse le montant restant");
        }

        // Créer un remboursement
        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmount(refundRequest.amount());
        refund.setReason(refundRequest.reason());

        try {
            // Le remboursement est maintenant géré via les webhooks Stripe
            // Nous enregistrons simplement le statut ici
            refund.setStatus(PaymentStatus.PENDING);

            // Si c'est un paiement par carte, il doit être traité par Stripe
            if (payment.getPaymentMethod() == PaymentMethod.CARTE_BANCAIRE) {
                log.info("Remboursement pour un paiement par carte en attente. Le statut sera mis à jour via webhook.");
            } else {
                // Pour les autres méthodes, marquer directement comme complété
                refund.setStatus(PaymentStatus.COMPLETED);
            }

            payment.addRefund(refund);

            // Mettre à jour le statut du paiement
            BigDecimal newTotalRefunded = totalRefundedAmount.add(refundRequest.amount());
            if (newTotalRefunded.compareTo(payment.getAmount()) >= 0) {
                payment.setStatus(PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }

            payment = paymentRepository.save(payment);

            return RefundResponse.builder()
                    .id(refund.getId())
                    .paymentId(payment.getId())
                    .amount(refund.getAmount())
                    .status(refund.getStatus())
                    .refundDate(refund.getRefundDate())
                    .reason(refund.getReason())
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors du traitement du remboursement: {}", e.getMessage());
            throw new PaymentException("Échec du remboursement: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByPaymentId(Long paymentId) {
        List<Refund> refunds = refundRepository.findByPaymentId(paymentId);

        return refunds.stream().map(refund ->
                RefundResponse.builder()
                        .id(refund.getId())
                        .paymentId(paymentId)
                        .amount(refund.getAmount())
                        .status(refund.getStatus())
                        .refundDate(refund.getRefundDate())
                        .reason(refund.getReason())
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Paiement non trouvé avec l'ID: " + paymentId));

        payment.setStatus(status);

        // Mettre à jour également le statut de la transaction principale
        if (!payment.getTransactions().isEmpty()) {
            payment.getTransactions().get(0).setStatus(status);
        }

        return paymentRepository.save(payment);
    }

    private String generateTransactionReference() {
        return "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}