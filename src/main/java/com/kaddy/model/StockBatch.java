package com.kaddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_batches", indexes = {@Index(name = "idx_stock_batch_item", columnList = "inventory_item_id"),
        @Index(name = "idx_stock_batch_hospital", columnList = "hospital_id"),
        @Index(name = "idx_stock_batch_expiry", columnList = "expiryDate"),
        @Index(name = "idx_stock_batch_number", columnList = "batchNumber")})
public class StockBatch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(nullable = false, length = 100)
    private String batchNumber;

    @NotNull
    @Column(nullable = false)
    private LocalDate manufacturingDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate expiryDate;

    @NotNull
    @Column(nullable = false)
    private Integer initialQuantity;

    @NotNull
    @Column(nullable = false)
    private Integer currentQuantity;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal purchasePrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    private LocalDate receivedDate;

    @Column(length = 500)
    private String notes;

    private Boolean isActive = true;

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isNearExpiry(int daysThreshold) {
        return LocalDate.now().plusDays(daysThreshold).isAfter(expiryDate);
    }

    public boolean hasStock() {
        return currentQuantity > 0;
    }
}
