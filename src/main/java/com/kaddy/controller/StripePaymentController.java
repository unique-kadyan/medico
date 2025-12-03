package com.kaddy.controller;

import com.kaddy.config.StripeConfig;
import com.kaddy.model.MedicineOrderPayment;
import com.kaddy.service.StripePaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;
    private final StripeConfig stripeConfig;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(Map.of("publishableKey", stripeConfig.getPublishableKey()));
    }

    @PostMapping("/create-payment-intent/{orderId}")
    public ResponseEntity<?> createPaymentIntent(@PathVariable Long orderId) {
        try {
            Map<String, String> response = stripePaymentService.createPaymentIntent(orderId);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Stripe error creating payment intent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create-checkout-session/{orderId}")
    public ResponseEntity<?> createCheckoutSession(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            String successUrl = request.get("successUrl");
            String cancelUrl = request.get("cancelUrl");

            Map<String, String> response = stripePaymentService.createCheckoutSession(orderId, successUrl, cancelUrl);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Stripe error creating checkout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, String> request) {
        try {
            Long orderId = Long.parseLong(request.get("orderId"));
            String paymentIntentId = request.get("paymentIntentId");
            String transactionId = request.get("transactionId");

            MedicineOrderPayment payment = stripePaymentService.confirmPayment(orderId, paymentIntentId, transactionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "receiptNumber", payment.getReceiptNumber(),
                    "message", "Payment confirmed successfully"));
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/payment-intent/{paymentIntentId}")
    public ResponseEntity<?> getPaymentIntentStatus(@PathVariable String paymentIntentId) {
        try {
            Map<String, Object> status = stripePaymentService.getPaymentIntentStatus(paymentIntentId);
            return ResponseEntity.ok(status);
        } catch (StripeException e) {
            log.error("Stripe error getting payment intent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            stripePaymentService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Webhook received");
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }
    }
}
