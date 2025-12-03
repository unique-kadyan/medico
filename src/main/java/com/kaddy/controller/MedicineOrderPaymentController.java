package com.kaddy.controller;

import com.kaddy.dto.MedicineOrderPaymentDTO;
import com.kaddy.dto.RecordPaymentRequest;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.MedicineOrderPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicine-order-payments")
@RequiredArgsConstructor
public class MedicineOrderPaymentController {

    private final MedicineOrderPaymentService paymentService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderPaymentDTO> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        Long receivedByUserId = securityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        MedicineOrderPaymentDTO payment = paymentService.recordPayment(request, receivedByUserId);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderPaymentDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/receipt/{receiptNumber}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderPaymentDTO> getPaymentByReceiptNumber(@PathVariable String receiptNumber) {
        return ResponseEntity.ok(paymentService.getPaymentByReceiptNumber(receiptNumber));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN', 'PATIENT')")
    public ResponseEntity<List<MedicineOrderPaymentDTO>> getPaymentsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }

    @GetMapping("/order/{orderId}/total-paid")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN', 'PATIENT')")
    public ResponseEntity<BigDecimal> getTotalPaidForOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getTotalPaidForOrder(orderId));
    }

    @GetMapping("/order/{orderId}/balance")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN', 'PATIENT')")
    public ResponseEntity<BigDecimal> getRemainingBalanceForOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getRemainingBalanceForOrder(orderId));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<MedicineOrderPaymentDTO>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(paymentService.getPaymentsByDateRange(startDate, endDate));
    }

    @GetMapping("/my-collections")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<MedicineOrderPaymentDTO>> getMyCollections() {
        Long userId = securityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderPaymentDTO> refundPayment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        BigDecimal refundAmount = new BigDecimal(request.get("refundAmount").toString());
        String reason = (String) request.get("reason");

        Long refundedByUserId = securityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        return ResponseEntity.ok(paymentService.refundPayment(id, refundAmount, reason, refundedByUserId));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderPaymentDTO> verifyPayment(@PathVariable Long id) {
        Long verifiedByUserId = securityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return ResponseEntity.ok(paymentService.verifyPayment(id, verifiedByUserId));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(paymentService.getPaymentSummary(startDate, endDate));
    }
}
