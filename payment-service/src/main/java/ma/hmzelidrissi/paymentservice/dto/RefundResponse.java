package ma.hmzelidrissi.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import ma.hmzelidrissi.paymentservice.domain.PaymentStatus;

@Builder
public record RefundResponse(
    Long id,
    Long paymentId,
    BigDecimal amount,
    PaymentStatus status,
    LocalDateTime refundDate,
    String reason) {}
