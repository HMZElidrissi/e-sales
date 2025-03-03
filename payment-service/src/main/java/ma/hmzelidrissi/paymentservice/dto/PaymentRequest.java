package ma.hmzelidrissi.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ma.hmzelidrissi.paymentservice.domain.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
    @NotBlank(message = "La référence de commande est obligatoire") String commandeReference,
    @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        BigDecimal amount,
    @NotNull(message = "La méthode de paiement est obligatoire") PaymentMethod paymentMethod,
    String cardNumber,
    String cardExpMonth,
    String cardExpYear,
    String cardCvc,
    String paypalEmail,
    String accountNumber,
    String bankCode) {}
