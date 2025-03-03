package ma.hmzelidrissi.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import ma.hmzelidrissi.paymentservice.domain.PaymentMethod;
import ma.hmzelidrissi.paymentservice.domain.PaymentStatus;

@Builder
public record PaymentResponse(
    Long id,
    String commandeReference,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    PaymentStatus status,
    LocalDateTime createdAt,
    String transactionReference) {}
