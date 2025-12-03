package com.kaddy.service;

import com.kaddy.config.StripeConfig;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.MedicineOrder;
import com.kaddy.model.MedicineOrderPayment;
import com.kaddy.model.enums.PaymentMethod;
import com.kaddy.model.enums.PaymentStatus;
import com.kaddy.repository.MedicineOrderPaymentRepository;
import com.kaddy.repository.MedicineOrderRepository;
import com.kaddy.security.SecurityUtils;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    private final StripeConfig stripeConfig;
    private final MedicineOrderRepository orderRepository;
    private final MedicineOrderPaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    public Map<String, String> createPaymentIntent(Long orderId) throws StripeException {
        log.info("Creating Stripe Payment Intent for order: {}", orderId);

        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        BigDecimal totalPaid = paymentRepository.getTotalPaidForOrder(orderId);
        if (totalPaid == null)
            totalPaid = BigDecimal.ZERO;
        BigDecimal remainingBalance = order.getFinalAmount().subtract(totalPaid);

        if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Order is already fully paid");
        }

        long amountInCents = remainingBalance.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .putMetadata("orderId", orderId.toString())
                .putMetadata("orderNumber", order.getOrderNumber())
                .putMetadata("patientName", order.getPatient().getFirstName() + " " + order.getPatient().getLastName())
                .setDescription("Medicine Order: " + order.getOrderNumber())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());
        response.put("paymentIntentId", paymentIntent.getId());
        response.put("amount", remainingBalance.toString());
        response.put("publishableKey", stripeConfig.getPublishableKey());

        log.info("Payment Intent created: {} for order: {}", paymentIntent.getId(), orderId);
        return response;
    }

    public Map<String, String> createCheckoutSession(Long orderId, String successUrl, String cancelUrl)
            throws StripeException {
        log.info("Creating Stripe Checkout Session for order: {}", orderId);

        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        BigDecimal totalPaid = paymentRepository.getTotalPaidForOrder(orderId);
        if (totalPaid == null)
            totalPaid = BigDecimal.ZERO;
        BigDecimal remainingBalance = order.getFinalAmount().subtract(totalPaid);

        if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Order is already fully paid");
        }

        long amountInCents = remainingBalance.multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Medicine Order: " + order.getOrderNumber())
                                                                .setDescription("Payment for medicine order")
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("orderId", orderId.toString())
                .putMetadata("orderNumber", order.getOrderNumber())
                .build();

        Session session = Session.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("url", session.getUrl());

        log.info("Checkout Session created: {} for order: {}", session.getId(), orderId);
        return response;
    }

    @Transactional
    public MedicineOrderPayment confirmPayment(Long orderId, String paymentIntentId, String transactionId) {
        log.info("Confirming payment for order: {}, paymentIntentId: {}", orderId, paymentIntentId);

        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        BigDecimal totalPaid = paymentRepository.getTotalPaidForOrder(orderId);
        if (totalPaid == null)
            totalPaid = BigDecimal.ZERO;
        BigDecimal amountPaid = order.getFinalAmount().subtract(totalPaid);

        MedicineOrderPayment payment = new MedicineOrderPayment();
        payment.setReceiptNumber(generateReceiptNumber());
        payment.setOrder(order);
        payment.setAmount(amountPaid);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId(transactionId != null ? transactionId : paymentIntentId);
        payment.setNotes("Online payment via Stripe");
        payment.setIsRefunded(false);
        payment.setIsVerified(true);
        payment.setVerifiedAt(LocalDateTime.now());

        securityUtils.getCurrentUserId().ifPresent(userId -> {
        });

        MedicineOrderPayment savedPayment = paymentRepository.save(payment);

        BigDecimal newTotalPaid = totalPaid.add(amountPaid);
        if (newTotalPaid.compareTo(order.getFinalAmount()) >= 0) {
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);
        }

        log.info("Payment confirmed for order: {}, receipt: {}", orderId, savedPayment.getReceiptNumber());
        return savedPayment;
    }

    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());

        log.info("Received Stripe webhook event: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailed(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (paymentIntent != null) {
            String orderId = paymentIntent.getMetadata().get("orderId");
            if (orderId != null) {
                confirmPayment(Long.parseLong(orderId), paymentIntent.getId(), paymentIntent.getId());
            }
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (session != null) {
            String orderId = session.getMetadata().get("orderId");
            if (orderId != null) {
                confirmPayment(Long.parseLong(orderId), session.getPaymentIntent(), session.getPaymentIntent());
            }
        }
    }

    private void handlePaymentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (paymentIntent != null) {
            String orderId = paymentIntent.getMetadata().get("orderId");
            log.error("Payment failed for order: {}, paymentIntent: {}", orderId, paymentIntent.getId());
            // Could update order status or send notification here
        }
    }

    public Map<String, Object> getPaymentIntentStatus(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", paymentIntent.getId());
        response.put("status", paymentIntent.getStatus());
        response.put("amount", paymentIntent.getAmount());
        response.put("currency", paymentIntent.getCurrency());

        return response;
    }

    private String generateReceiptNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "RCP-" + timestamp + "-" + random;
    }
}
