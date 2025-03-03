package ma.hmzelidrissi.paymentservice.service;

import com.stripe.exception.StripeException;
import ma.hmzelidrissi.paymentservice.dto.CheckoutRequest;
import ma.hmzelidrissi.paymentservice.dto.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse createCheckoutSession(CheckoutRequest checkoutRequest) throws StripeException;
}