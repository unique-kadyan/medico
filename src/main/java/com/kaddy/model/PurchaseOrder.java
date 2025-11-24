package com.kaddy.model;

import com.kaddy.model.enums.PurchaseOrderStatus;
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
@Table(name = "purchase_orders", indexes = {@Index(name = "idx_po_number", columnList = "poNumber", unique = true),
        @Index(name = "idx_po_hospital", columnList = "hospital_id"),
        @Index(name = "idx_po_vendor", columnList = "vendor_id"),
        @Index(name = "idx_po_status", columnList = "status")})
public class PurchaseOrder extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String poNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @NotNull
    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private LocalDate actualDeliveryDate;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal shippingCharges = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    private LocalDate paymentDueDate;

    private String deliveryAddress;
    private String deliveryContactPerson;
    private String deliveryContactPhone;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private LocalDateTime approvedAt;

    @Column(length = 500)
    private String approvalNotes;

    private String quotationNumber;
    private String invoiceNumber;
    private String grnNumber;

    @Column(length = 1000)
    private String notes;

    @Column(length = 500)
    private String termsAndConditions;

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotal = items.stream().map(PurchaseOrderItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = items.stream().map(PurchaseOrderItem::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = this.subtotal.add(this.taxAmount)
                .add(this.shippingCharges != null ? this.shippingCharges : BigDecimal.ZERO)
                .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);

        this.balanceAmount = this.totalAmount.subtract(this.paidAmount);
    }
}
