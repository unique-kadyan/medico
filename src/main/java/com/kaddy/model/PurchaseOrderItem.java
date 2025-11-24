package com.kaddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_order_items", indexes = {@Index(name = "idx_po_item_order", columnList = "purchase_order_id"),
        @Index(name = "idx_po_item_inventory", columnList = "inventory_item_id")})
public class PurchaseOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(nullable = false)
    private String itemName;

    @Column(length = 500)
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer orderedQuantity;

    @Column(nullable = false)
    private Integer receivedQuantity = 0;

    @Column(nullable = false)
    private Integer pendingQuantity;

    @Column(length = 20)
    private String unit;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(length = 50)
    private String hsnCode;

    @Column(length = 500)
    private String notes;

    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        this.pendingQuantity = this.orderedQuantity - this.receivedQuantity;

        BigDecimal baseAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.orderedQuantity));

        if (this.discountPercentage != null && this.discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = baseAmount.multiply(this.discountPercentage).divide(BigDecimal.valueOf(100), 2,
                    RoundingMode.HALF_UP);
        }

        BigDecimal afterDiscount = baseAmount.subtract(this.discountAmount);

        if (this.taxRate != null && this.taxRate.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = afterDiscount.multiply(this.taxRate).divide(BigDecimal.valueOf(100), 2,
                    RoundingMode.HALF_UP);
        }

        this.lineTotal = afterDiscount.add(this.taxAmount);
    }
}
