package ma.hmzelidrissi.paymentservice.service.Impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hmzelidrissi.paymentservice.dto.CheckoutRequest;
import ma.hmzelidrissi.paymentservice.dto.CheckoutResponse;
import ma.hmzelidrissi.paymentservice.service.CheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public CheckoutResponse createCheckoutSession(CheckoutRequest checkoutRequest) throws StripeException {
        log.info("Création d'une session de paiement Stripe pour la commande: {}", checkoutRequest.commandeReference());

        // Créer une session Stripe Checkout
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/payment-cancel?session_id={CHECKOUT_SESSION_ID}")
                .putMetadata("commandeReference", checkoutRequest.commandeReference())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(checkoutRequest.amount().multiply(java.math.BigDecimal.valueOf(100)).longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Commande #" + checkoutRequest.commandeReference())
                                                                .setDescription("Paiement pour la commande #" + checkoutRequest.commandeReference())
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .build();

        Session session = Session.create(params);
        log.info("Session Stripe Checkout créée avec succès: {}", session.getId());

        return new CheckoutResponse(session.getId(), session.getUrl());
    }
}