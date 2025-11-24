package com.kaddy.model;

import com.kaddy.model.enums.BillingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices", indexes = {@Index(name = "idx_invoice_number", columnList = "invoiceNumber", unique = true),
        @Index(name = "idx_invoice_hospital", columnList = "hospital_id"),
        @Index(name = "idx_invoice_patient", columnList = "patient_id"),
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_date", columnList = "invoiceDate")})
public class Invoice extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id")
    private PatientAdmission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @NotNull
    @Column(nullable = false)
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingStatus status = BillingStatus.DRAFT;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(length = 200)
    private String discountReason;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal insuranceCoveredAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal patientResponsibility = BigDecimal.ZERO;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime finalizedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finalized_by")
    private User finalizedBy;

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
        recalculateTotals();
    }

    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotal = items.stream().map(InvoiceItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = items.stream().map(InvoiceItem::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmt = this.discountAmount;
        if (this.discountPercentage != null && this.discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            discountAmt = this.subtotal.multiply(this.discountPercentage).divide(BigDecimal.valueOf(100));
        }

        this.totalAmount = this.subtotal.add(this.taxAmount).subtract(discountAmt);
        this.balanceAmount = this.totalAmount.subtract(this.paidAmount).subtract(this.insuranceCoveredAmount);
        this.patientResponsibility = this.totalAmount.subtract(this.insuranceCoveredAmount);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setInvoice(this);
        this.paidAmount = this.paidAmount.add(payment.getAmount());
        this.balanceAmount = this.totalAmount.subtract(this.paidAmount).subtract(this.insuranceCoveredAmount);
        updateStatus();
    }

    private void updateStatus() {
        if (this.balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = BillingStatus.PAID;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = BillingStatus.PARTIAL_PAID;
        }
    }
}
