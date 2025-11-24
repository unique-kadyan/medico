package com.kaddy.model;

import com.kaddy.model.enums.ServiceCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice_items", indexes = {@Index(name = "idx_invoice_item_invoice", columnList = "invoice_id"),
        @Index(name = "idx_invoice_item_service", columnList = "service_item_id")})
public class InvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_item_id")
    private ServiceItem serviceItem;

    @NotBlank
    @Column(nullable = false)
    private String itemName;

    @Column(length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceCategory category;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(length = 20)
    private String unit;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    private LocalDate serviceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor performedBy;

    @Column(length = 100)
    private String referenceType;

    private Long referenceId;

    @Column(length = 50)
    private String hsnSacCode;

    private Boolean isInsuranceCovered = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal insuranceApprovedAmount = BigDecimal.ZERO;

    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        BigDecimal baseAmount = this.unitPrice.multiply(this.quantity);
        if (this.taxRate != null && this.taxRate.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = baseAmount.multiply(this.taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        this.lineTotal = baseAmount.add(this.taxAmount);
    }
}
