package ma.hmzelidrissi.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RefundRequest(
    @NotNull(message = "L'ID du paiement est obligatoire") Long paymentId,
    @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        BigDecimal amount,
    @NotBlank(message = "La raison du remboursement est obligatoire") String reason) {}
