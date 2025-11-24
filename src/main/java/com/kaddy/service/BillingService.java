package com.kaddy.service;

import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.*;
import com.kaddy.model.enums.BillingStatus;
import com.kaddy.model.enums.PaymentMethod;
import com.kaddy.model.enums.ServiceCategory;
import com.kaddy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    public Invoice createInvoice(Long hospitalId, Long patientId, Long createdByUserId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber(hospitalId));
        invoice.setHospital(hospital);
        invoice.setPatient(patient);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(BillingStatus.DRAFT);
        invoice.setCreatedBy(createdBy);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Created invoice {} for patient {} at hospital {}", savedInvoice.getInvoiceNumber(), patientId,
                hospitalId);
        return savedInvoice;
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceNumber));
    }

    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesByHospital(Long hospitalId, Pageable pageable) {
        return invoiceRepository.findByHospitalId(hospitalId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesByHospitalAndStatus(Long hospitalId, BillingStatus status, Pageable pageable) {
        return invoiceRepository.findByHospitalIdAndStatus(hospitalId, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByPatient(Long patientId) {
        return invoiceRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> searchInvoices(Long hospitalId, String search, Pageable pageable) {
        return invoiceRepository.searchInvoices(hospitalId, search, pageable);
    }

    public Invoice addItemToInvoice(Long invoiceId, Long serviceItemId, BigDecimal quantity, LocalDate serviceDate,
            Long doctorId, String notes) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() != BillingStatus.DRAFT) {
            throw new IllegalStateException("Cannot add items to a finalized invoice");
        }

        ServiceItem serviceItem = serviceItemRepository.findById(serviceItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found"));

        InvoiceItem item = new InvoiceItem();
        item.setInvoice(invoice);
        item.setServiceItem(serviceItem);
        item.setItemName(serviceItem.getName());
        item.setDescription(serviceItem.getDescription());
        item.setCategory(serviceItem.getCategory());
        item.setUnitPrice(serviceItem.getUnitPrice());
        item.setQuantity(quantity);
        item.setUnit(serviceItem.getUnit());
        item.setTaxRate(serviceItem.getTaxRate() != null ? serviceItem.getTaxRate() : BigDecimal.ZERO);
        item.setHsnSacCode(serviceItem.getHsnSacCode());
        item.setServiceDate(serviceDate != null ? serviceDate : LocalDate.now());

        invoice.addItem(item);
        return invoiceRepository.save(invoice);
    }

    public Invoice addCustomItemToInvoice(Long invoiceId, String itemName, ServiceCategory category,
            BigDecimal unitPrice, BigDecimal quantity, String unit, BigDecimal taxRate, String description) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() != BillingStatus.DRAFT) {
            throw new IllegalStateException("Cannot add items to a finalized invoice");
        }

        InvoiceItem item = new InvoiceItem();
        item.setInvoice(invoice);
        item.setItemName(itemName);
        item.setCategory(category);
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setTaxRate(taxRate != null ? taxRate : BigDecimal.ZERO);
        item.setDescription(description);
        item.setServiceDate(LocalDate.now());

        invoice.addItem(item);
        return invoiceRepository.save(invoice);
    }

    public Invoice removeItemFromInvoice(Long invoiceId, Long itemId) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() != BillingStatus.DRAFT) {
            throw new IllegalStateException("Cannot remove items from a finalized invoice");
        }

        invoice.getItems().removeIf(item -> item.getId().equals(itemId));
        invoice.recalculateTotals();
        return invoiceRepository.save(invoice);
    }

    public Invoice applyDiscount(Long invoiceId, BigDecimal discountAmount, BigDecimal discountPercentage,
            String reason) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() != BillingStatus.DRAFT) {
            throw new IllegalStateException("Cannot apply discount to a finalized invoice");
        }

        invoice.setDiscountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        invoice.setDiscountPercentage(discountPercentage != null ? discountPercentage : BigDecimal.ZERO);
        invoice.setDiscountReason(reason);
        invoice.recalculateTotals();

        return invoiceRepository.save(invoice);
    }

    public Invoice finalizeInvoice(Long invoiceId, Long finalizedByUserId) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() != BillingStatus.DRAFT) {
            throw new IllegalStateException("Invoice is already finalized");
        }

        if (invoice.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot finalize an empty invoice");
        }

        User finalizedBy = userRepository.findById(finalizedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        invoice.setStatus(BillingStatus.PENDING);
        invoice.setFinalizedAt(LocalDateTime.now());
        invoice.setFinalizedBy(finalizedBy);
        invoice.recalculateTotals();

        log.info("Invoice {} finalized by user {}", invoice.getInvoiceNumber(), finalizedByUserId);
        return invoiceRepository.save(invoice);
    }

    public Invoice cancelInvoice(Long invoiceId, String reason) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() == BillingStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a fully paid invoice");
        }

        invoice.setStatus(BillingStatus.CANCELLED);
        invoice.setNotes(invoice.getNotes() != null
                ? invoice.getNotes() + "\nCancellation reason: " + reason
                : "Cancellation reason: " + reason);

        log.info("Invoice {} cancelled. Reason: {}", invoice.getInvoiceNumber(), reason);
        return invoiceRepository.save(invoice);
    }

    public Payment recordPayment(Long invoiceId, BigDecimal amount, PaymentMethod paymentMethod, String transactionId,
            Long receivedByUserId, String notes) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() == BillingStatus.DRAFT) {
            throw new IllegalStateException("Cannot record payment for a draft invoice");
        }

        if (invoice.getStatus() == BillingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot record payment for a cancelled invoice");
        }

        if (invoice.getStatus() == BillingStatus.PAID) {
            throw new IllegalStateException("Invoice is already fully paid");
        }

        if (amount.compareTo(invoice.getBalanceAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds balance amount");
        }

        User receivedBy = userRepository.findById(receivedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Payment payment = new Payment();
        payment.setReceiptNumber(generateReceiptNumber(invoice.getHospital().getId()));
        payment.setHospital(invoice.getHospital());
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId(transactionId);
        payment.setReceivedBy(receivedBy);
        payment.setNotes(notes);

        Payment savedPayment = paymentRepository.save(payment);
        invoice.addPayment(savedPayment);
        invoiceRepository.save(invoice);

        log.info("Payment {} of {} recorded for invoice {}", savedPayment.getReceiptNumber(), amount,
                invoice.getInvoiceNumber());
        return savedPayment;
    }

    public Payment refundPayment(Long paymentId, BigDecimal refundAmount, String reason, Long refundedByUserId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getIsRefunded()) {
            throw new IllegalStateException("Payment is already refunded");
        }

        BigDecimal maxRefund = payment.getAmount().subtract(payment.getRefundedAmount());
        if (refundAmount.compareTo(maxRefund) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds available amount");
        }

        User refundedBy = userRepository.findById(refundedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        payment.setRefundedAmount(payment.getRefundedAmount().add(refundAmount));
        payment.setRefundReason(reason);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundedBy(refundedBy);

        if (payment.getRefundedAmount().compareTo(payment.getAmount()) >= 0) {
            payment.setIsRefunded(true);
        }

        Invoice invoice = payment.getInvoice();
        invoice.setPaidAmount(invoice.getPaidAmount().subtract(refundAmount));
        invoice.recalculateTotals();

        if (invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(BillingStatus.PARTIAL_PAID);
        }

        invoiceRepository.save(invoice);
        log.info("Refund of {} processed for payment {}", refundAmount, payment.getReceiptNumber());
        return paymentRepository.save(payment);
    }

    public ServiceItem createServiceItem(Long hospitalId, String code, String name, ServiceCategory category,
            BigDecimal unitPrice, String unit, BigDecimal taxRate, String description) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));

        if (serviceItemRepository.existsByHospitalIdAndCode(hospitalId, code)) {
            throw new IllegalArgumentException("Service item with code " + code + " already exists");
        }

        ServiceItem item = new ServiceItem();
        item.setHospital(hospital);
        item.setCode(code);
        item.setName(name);
        item.setCategory(category);
        item.setUnitPrice(unitPrice);
        item.setUnit(unit);
        item.setTaxRate(taxRate);
        item.setDescription(description);
        item.setIsActive(true);

        return serviceItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<ServiceItem> getServiceItemsByHospital(Long hospitalId) {
        return serviceItemRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
    }

    @Transactional(readOnly = true)
    public List<ServiceItem> getServiceItemsByCategory(Long hospitalId, ServiceCategory category) {
        return serviceItemRepository.findByHospitalIdAndCategoryAndIsActiveTrue(hospitalId, category);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueReport(Long hospitalId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalRevenue = invoiceRepository.getTotalRevenue(hospitalId, startDate, endDate);
        BigDecimal totalCollected = invoiceRepository.getTotalCollected(hospitalId, startDate, endDate);
        BigDecimal totalOutstanding = invoiceRepository.getTotalOutstanding(hospitalId);

        Long pendingInvoices = invoiceRepository.countByHospitalIdAndStatus(hospitalId, BillingStatus.PENDING);
        Long overdueInvoices = invoiceRepository.countByHospitalIdAndStatus(hospitalId, BillingStatus.OVERDUE);
        Long paidInvoices = invoiceRepository.countByHospitalIdAndStatus(hospitalId, BillingStatus.PAID);

        return Map.of("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO, "totalCollected",
                totalCollected != null ? totalCollected : BigDecimal.ZERO, "totalOutstanding",
                totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO, "pendingInvoices", pendingInvoices,
                "overdueInvoices", overdueInvoices, "paidInvoices", paidInvoices, "startDate", startDate, "endDate",
                endDate);
    }

    private String generateInvoiceNumber(Long hospitalId) {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        String lastNumber = invoiceRepository.findLastInvoiceNumber(hospitalId, prefix);

        int nextNumber = 1;
        if (lastNumber != null) {
            String numPart = lastNumber.substring(prefix.length());
            nextNumber = Integer.parseInt(numPart) + 1;
        }

        return prefix + String.format("%05d", nextNumber);
    }

    private String generateReceiptNumber(Long hospitalId) {
        String prefix = "RCP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        String lastNumber = paymentRepository.findLastReceiptNumber(hospitalId, prefix);

        int nextNumber = 1;
        if (lastNumber != null) {
            String numPart = lastNumber.substring(prefix.length());
            nextNumber = Integer.parseInt(numPart) + 1;
        }

        return prefix + String.format("%05d", nextNumber);
    }
}
