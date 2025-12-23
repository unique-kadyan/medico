package com.kaddy.controller;

import com.kaddy.service.RazorpayPaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/razorpay")
@RequiredArgsConstructor
@Slf4j
public class RazorpayPaymentController {

    private final RazorpayPaymentService razorpayPaymentService;

    @PostMapping("/create-order/{orderId}")
    public ResponseEntity<?> createOrder(@PathVariable Long orderId) {
        try {
            Map<String, Object> response = razorpayPaymentService.createOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for medicine order: {}", orderId, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to create payment order",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error creating Razorpay order: {}", orderId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Internal server error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> paymentData) {
        try {
            String razorpayOrderId = paymentData.get("razorpay_order_id");
            String razorpayPaymentId = paymentData.get("razorpay_payment_id");
            String razorpaySignature = paymentData.get("razorpay_signature");

            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Missing required payment verification parameters"
                ));
            }

            Map<String, Object> response = razorpayPaymentService.verifyPayment(
                    razorpayOrderId, razorpayPaymentId, razorpaySignature);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Payment verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Payment verification failed",
                    "message", e.getMessage()
            ));
        } catch (RazorpayException e) {
            log.error("Razorpay error during verification: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Payment verification error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error during payment verification", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Internal server error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            razorpayPaymentService.handleWebhook(payload, signature);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Webhook processing failed",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/status/{razorpayOrderId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String razorpayOrderId) {
        try {
            Map<String, Object> response = razorpayPaymentService.getPaymentStatus(razorpayOrderId);
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            log.error("Failed to fetch payment status for order: {}", razorpayOrderId, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to fetch payment status",
                    "message", e.getMessage()
            ));
        }
    }
}
