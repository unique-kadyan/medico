package com.kaddy.controller;

import com.kaddy.dto.CreateMedicineOrderRequest;
import com.kaddy.dto.MedicineOrderDTO;
import com.kaddy.model.enums.MedicineOrderStatus;
import com.kaddy.model.enums.PaymentStatus;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.MedicineOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicine-orders")
@RequiredArgsConstructor
public class MedicineOrderController {

    private final MedicineOrderService orderService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderDTO> createOrder(@Valid @RequestBody CreateMedicineOrderRequest request) {
        MedicineOrderDTO createdOrder = orderService.createOrder(request);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<MedicineOrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineOrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<MedicineOrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicineOrderDTO>> getOrdersByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(orderService.getOrdersByPatientId(patientId));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicineOrderDTO>> getMyOrders() {
        Long userId = securityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return ResponseEntity.ok(orderService.getOrdersByPatientUserId(userId));
    }

    @GetMapping("/prescription/{prescriptionId}")
    public ResponseEntity<List<MedicineOrderDTO>> getOrdersByPrescriptionId(@PathVariable Long prescriptionId) {
        return ResponseEntity.ok(orderService.getOrdersByPrescriptionId(prescriptionId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<MedicineOrderDTO>> getOrdersByStatus(@PathVariable MedicineOrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<MedicineOrderDTO>> getPendingOrders() {
        return ResponseEntity.ok(orderService.getOrdersByStatus(MedicineOrderStatus.PENDING));
    }

    @GetMapping("/payment-pending")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<MedicineOrderDTO>> getPaymentPendingOrders() {
        return ResponseEntity.ok(orderService.getOrdersByPaymentStatus(PaymentStatus.PENDING));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        MedicineOrderStatus status = MedicineOrderStatus.valueOf(request.get("status"));
        Long processedById = securityUtils.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status, processedById));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderDTO> confirmOrder(@PathVariable Long id) {
        Long processedById = securityUtils.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, MedicineOrderStatus.CONFIRMED, processedById));
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderDTO> processOrder(@PathVariable Long id) {
        Long processedById = securityUtils.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, MedicineOrderStatus.PROCESSING, processedById));
    }

    @PutMapping("/{id}/ready")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderDTO> markReadyForPickup(@PathVariable Long id) {
        Long processedById = securityUtils.getCurrentUserId().orElse(null);
        return ResponseEntity
                .ok(orderService.updateOrderStatus(id, MedicineOrderStatus.READY_FOR_PICKUP, processedById));
    }

    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderDTO> deliverOrder(@PathVariable Long id) {
        Long processedById = securityUtils.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, MedicineOrderStatus.DELIVERED, processedById));
    }

    @PutMapping("/{id}/payment-status")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<MedicineOrderDTO> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        PaymentStatus paymentStatus = PaymentStatus.valueOf(request.get("paymentStatus"));
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, paymentStatus));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<MedicineOrderDTO> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, PaymentStatus.PAID));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<MedicineOrderDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/pending")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<Long> countPendingOrders() {
        return ResponseEntity.ok(orderService.countOrdersByStatus(MedicineOrderStatus.PENDING));
    }
}
