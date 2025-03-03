package ma.hmzelidrissi.paymentservice.service;

import ma.hmzelidrissi.paymentservice.domain.Payment;
import ma.hmzelidrissi.paymentservice.domain.PaymentStatus;
import ma.hmzelidrissi.paymentservice.dto.PaymentRequest;
import ma.hmzelidrissi.paymentservice.dto.PaymentResponse;
import ma.hmzelidrissi.paymentservice.dto.RefundRequest;
import ma.hmzelidrissi.paymentservice.dto.RefundResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest paymentRequest);
    PaymentResponse getPaymentById(Long id);
    List<PaymentResponse> getPaymentsByCommandeReference(String commandeReference);
    RefundResponse processRefund(RefundRequest refundRequest);
    List<RefundResponse> getRefundsByPaymentId(Long paymentId);
    Payment updatePaymentStatus(Long paymentId, PaymentStatus status);
}