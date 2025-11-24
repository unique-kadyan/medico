package com.kaddy.controller;

import com.kaddy.model.Invoice;
import com.kaddy.model.Payment;
import com.kaddy.model.ServiceItem;
import com.kaddy.model.User;
import com.kaddy.model.enums.BillingStatus;
import com.kaddy.model.enums.PaymentMethod;
import com.kaddy.model.enums.ServiceCategory;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    private final BillingService billingService;
    private final SecurityUtils securityUtils;

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Invoice> createInvoice(@RequestParam Long patientId) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital() != null ? currentUser.getHospital().getId() : null;
        if (hospitalId == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }

        Invoice invoice = billingService.createInvoice(hospitalId, patientId, currentUser.getId());
        return new ResponseEntity<>(invoice, HttpStatus.CREATED);
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoiceById(id));
    }

    @GetMapping("/invoices/number/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(billingService.getInvoiceByNumber(invoiceNumber));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Page<Invoice>> getInvoices(@RequestParam(required = false) BillingStatus status,
            @RequestParam(required = false) String search, @PageableDefault(size = 20) Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital().getId();

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(billingService.searchInvoices(hospitalId, search, pageable));
        } else if (status != null) {
            return ResponseEntity.ok(billingService.getInvoicesByHospitalAndStatus(hospitalId, status, pageable));
        } else {
            return ResponseEntity.ok(billingService.getInvoicesByHospital(hospitalId, pageable));
        }
    }

    @GetMapping("/invoices/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<Invoice>> getInvoicesByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getInvoicesByPatient(patientId));
    }

    @PostMapping("/invoices/{invoiceId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Invoice> addItemToInvoice(@PathVariable Long invoiceId, @RequestParam Long serviceItemId,
            @RequestParam(defaultValue = "1") BigDecimal quantity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate serviceDate,
            @RequestParam(required = false) Long doctorId, @RequestParam(required = false) String notes) {
        Invoice invoice = billingService.addItemToInvoice(invoiceId, serviceItemId, quantity, serviceDate, doctorId,
                notes);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices/{invoiceId}/items/custom")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Invoice> addCustomItemToInvoice(@PathVariable Long invoiceId, @RequestParam String itemName,
            @RequestParam ServiceCategory category, @RequestParam BigDecimal unitPrice,
            @RequestParam(defaultValue = "1") BigDecimal quantity, @RequestParam(required = false) String unit,
            @RequestParam(required = false) BigDecimal taxRate, @RequestParam(required = false) String description) {
        Invoice invoice = billingService.addCustomItemToInvoice(invoiceId, itemName, category, unitPrice, quantity,
                unit, taxRate, description);
        return ResponseEntity.ok(invoice);
    }

    @DeleteMapping("/invoices/{invoiceId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Invoice> removeItemFromInvoice(@PathVariable Long invoiceId, @PathVariable Long itemId) {
        Invoice invoice = billingService.removeItemFromInvoice(invoiceId, itemId);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices/{invoiceId}/discount")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Invoice> applyDiscount(@PathVariable Long invoiceId,
            @RequestParam(required = false) BigDecimal discountAmount,
            @RequestParam(required = false) BigDecimal discountPercentage,
            @RequestParam(required = false) String reason) {
        Invoice invoice = billingService.applyDiscount(invoiceId, discountAmount, discountPercentage, reason);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices/{invoiceId}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Invoice> finalizeInvoice(@PathVariable Long invoiceId) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Invoice invoice = billingService.finalizeInvoice(invoiceId, currentUser.getId());
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices/{invoiceId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Invoice> cancelInvoice(@PathVariable Long invoiceId, @RequestParam String reason) {
        Invoice invoice = billingService.cancelInvoice(invoiceId, reason);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices/{invoiceId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Payment> recordPayment(@PathVariable Long invoiceId, @RequestParam BigDecimal amount,
            @RequestParam PaymentMethod paymentMethod, @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String notes) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Payment payment = billingService.recordPayment(invoiceId, amount, paymentMethod, transactionId,
                currentUser.getId(), notes);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @PostMapping("/payments/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Payment> refundPayment(@PathVariable Long paymentId, @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Payment payment = billingService.refundPayment(paymentId, amount, reason, currentUser.getId());
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/service-items")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ServiceItem> createServiceItem(@RequestParam String code, @RequestParam String name,
            @RequestParam ServiceCategory category, @RequestParam BigDecimal unitPrice,
            @RequestParam(required = false) String unit, @RequestParam(required = false) BigDecimal taxRate,
            @RequestParam(required = false) String description) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital().getId();
        ServiceItem item = billingService.createServiceItem(hospitalId, code, name, category, unitPrice, unit, taxRate,
                description);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @GetMapping("/service-items")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<ServiceItem>> getServiceItems(@RequestParam(required = false) ServiceCategory category) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital().getId();

        if (category != null) {
            return ResponseEntity.ok(billingService.getServiceItemsByCategory(hospitalId, category));
        } else {
            return ResponseEntity.ok(billingService.getServiceItemsByHospital(hospitalId));
        }
    }

    @GetMapping("/reports/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital().getId();
        Map<String, Object> report = billingService.getRevenueReport(hospitalId, startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
