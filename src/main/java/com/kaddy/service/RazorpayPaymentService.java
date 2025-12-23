package com.kaddy.service;

import com.kaddy.config.RazorpayConfig;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.MedicineOrder;
import com.kaddy.model.MedicineOrderPayment;
import com.kaddy.model.User;
import com.kaddy.model.enums.PaymentMethod;
import com.kaddy.model.enums.PaymentStatus;
import com.kaddy.repository.MedicineOrderPaymentRepository;
import com.kaddy.repository.MedicineOrderRepository;
import com.kaddy.security.SecurityUtils;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayPaymentService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;
    private final MedicineOrderRepository orderRepository;
    private final MedicineOrderPaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    // Store razorpay order ID to medicine order ID mapping for verification
    private final ConcurrentHashMap<String, Long> razorpayOrderMapping = new ConcurrentHashMap<>();

    @Transactional
    public Map<String, Object> createOrder(Long orderId) throws RazorpayException {
        MedicineOrder medicineOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine order not found: " + orderId));

        BigDecimal amount = medicineOrder.getTotalAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid order amount");
        }

        // Razorpay expects amount in paise (smallest currency unit)
        int amountInPaise = amount.multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_" + orderId);
        orderRequest.put("notes", new JSONObject()
                .put("medicine_order_id", orderId.toString())
                .put("patient_id", medicineOrder.getPatient().getPatientId()));

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        log.info("Created Razorpay order: {} for medicine order: {}", razorpayOrderId, orderId);

        // Store mapping for later verification
        razorpayOrderMapping.put(razorpayOrderId, orderId);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", razorpayOrderId);
        response.put("amount", amountInPaise);
        response.put("currency", "INR");
        response.put("keyId", razorpayConfig.getKeyId());
        response.put("medicineOrderId", orderId);
        response.put("patientName", medicineOrder.getPatient().getFirstName() + " " + medicineOrder.getPatient().getLastName());
        response.put("patientEmail", medicineOrder.getPatient().getEmail());
        response.put("patientPhone", medicineOrder.getPatient().getPhone());

        return response;
    }

    @Transactional
    public Map<String, Object> verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws RazorpayException {
        // Verify signature
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        boolean isValid = Utils.verifySignature(payload, razorpaySignature, razorpayConfig.getKeySecret());

        if (!isValid) {
            log.error("Invalid Razorpay signature for order: {}", razorpayOrderId);
            throw new IllegalArgumentException("Invalid payment signature");
        }

        // Get payment details from Razorpay
        Payment payment = razorpayClient.payments.fetch(razorpayPaymentId);

        // Get the medicine order ID from mapping
        Long mappedOrderId = razorpayOrderMapping.get(razorpayOrderId);
        final Long medicineOrderId;
        if (mappedOrderId == null) {
            // Try to extract from Razorpay order notes
            Order razorpayOrder = razorpayClient.orders.fetch(razorpayOrderId);
            JSONObject notes = razorpayOrder.get("notes");
            if (notes != null && notes.has("medicine_order_id")) {
                medicineOrderId = Long.parseLong(notes.getString("medicine_order_id"));
            } else {
                throw new ResourceNotFoundException("Medicine order mapping not found for Razorpay order: " + razorpayOrderId);
            }
        } else {
            medicineOrderId = mappedOrderId;
        }

        MedicineOrder medicineOrder = orderRepository.findById(medicineOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine order not found: " + medicineOrderId));

        String status = payment.get("status");
        Map<String, Object> response = new HashMap<>();

        if ("captured".equals(status)) {
            // Create payment record
            MedicineOrderPayment orderPayment = new MedicineOrderPayment();
            orderPayment.setOrder(medicineOrder);
            orderPayment.setAmount(medicineOrder.getTotalAmount());
            orderPayment.setPaymentDate(LocalDateTime.now());
            orderPayment.setTransactionId(razorpayPaymentId);
            orderPayment.setReceiptNumber(generateReceiptNumber());

            // Determine payment method from Razorpay response
            String method = payment.get("method");
            if ("upi".equals(method)) {
                orderPayment.setPaymentMethod(PaymentMethod.UPI);
                Object vpa = payment.get("vpa");
                if (vpa != null) {
                    orderPayment.setUpiId(vpa.toString());
                }
            } else if ("card".equals(method)) {
                orderPayment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
                JSONObject card = payment.get("card");
                if (card != null && card.has("last4")) {
                    orderPayment.setCardLastFourDigits(card.getString("last4"));
                }
            } else if ("netbanking".equals(method)) {
                orderPayment.setPaymentMethod(PaymentMethod.NET_BANKING);
                Object bank = payment.get("bank");
                if (bank != null) {
                    orderPayment.setBankName(bank.toString());
                }
            } else if ("wallet".equals(method)) {
                orderPayment.setPaymentMethod(PaymentMethod.WALLET);
            } else {
                orderPayment.setPaymentMethod(PaymentMethod.OTHER);
            }

            // Set received by current user
            Optional<User> currentUser = securityUtils.getCurrentUser();
            currentUser.ifPresent(orderPayment::setReceivedBy);

            paymentRepository.save(orderPayment);

            // Update medicine order payment status
            medicineOrder.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(medicineOrder);

            // Clean up mapping
            razorpayOrderMapping.remove(razorpayOrderId);

            log.info("Payment successful for order: {}, payment ID: {}, method: {}",
                    razorpayOrderId, razorpayPaymentId, method);

            response.put("success", true);
            response.put("receiptNumber", orderPayment.getReceiptNumber());
        } else {
            log.warn("Payment not captured for order: {}, status: {}", razorpayOrderId, status);
            response.put("success", false);
        }

        response.put("paymentId", razorpayPaymentId);
        response.put("orderId", razorpayOrderId);
        response.put("status", status);

        return response;
    }

    @Transactional
    public void handleWebhook(String payload, String signature) throws RazorpayException {
        // Verify webhook signature
        boolean isValid = Utils.verifyWebhookSignature(payload, signature, razorpayConfig.getWebhookSecret());

        if (!isValid) {
            log.error("Invalid webhook signature");
            throw new IllegalArgumentException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        log.info("Received Razorpay webhook: {}", eventType);

        if ("payment.captured".equals(eventType)) {
            JSONObject paymentEntity = event.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayOrderId = paymentEntity.getString("order_id");

            // Get medicine order ID from mapping
            Long medicineOrderId = razorpayOrderMapping.get(razorpayOrderId);
            if (medicineOrderId != null) {
                MedicineOrder medicineOrder = orderRepository.findById(medicineOrderId).orElse(null);
                if (medicineOrder != null && medicineOrder.getPaymentStatus() != PaymentStatus.PAID) {
                    medicineOrder.setPaymentStatus(PaymentStatus.PAID);
                    orderRepository.save(medicineOrder);
                    log.info("Webhook: Payment captured for medicine order: {}", medicineOrderId);
                }
            }
        }
    }

    public Map<String, Object> getPaymentStatus(String razorpayOrderId) throws RazorpayException {
        Order order = razorpayClient.orders.fetch(razorpayOrderId);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", razorpayOrderId);
        response.put("status", order.get("status"));
        response.put("amountPaid", order.get("amount_paid"));
        response.put("amountDue", order.get("amount_due"));

        return response;
    }

    private String generateReceiptNumber() {
        String prefix = "RZP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastReceipt = paymentRepository.findLastReceiptNumber(prefix);

        int sequence = 1;
        if (lastReceipt != null && lastReceipt.length() > prefix.length()) {
            try {
                sequence = Integer.parseInt(lastReceipt.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                log.warn("Could not parse receipt sequence from: {}", lastReceipt);
            }
        }

        return prefix + String.format("%04d", sequence);
    }
}
