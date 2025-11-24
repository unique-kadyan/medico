package com.kaddy.model;

import com.kaddy.model.enums.InventoryCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory_items", indexes = {@Index(name = "idx_inventory_item_hospital", columnList = "hospital_id"),
        @Index(name = "idx_inventory_item_sku", columnList = "sku"),
        @Index(name = "idx_inventory_item_category", columnList = "category"),
        @Index(name = "idx_inventory_item_barcode", columnList = "barcode")})
public class InventoryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String sku;

    @Column(length = 50)
    private String barcode;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 200)
    private String genericName;

    @Column(length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryCategory category;

    @Column(length = 200)
    private String manufacturer;

    @Column(length = 100)
    private String batchNumber;

    @Column(length = 50)
    private String strength;

    @Column(length = 50)
    private String dosageForm;

    private Boolean requiresPrescription = false;

    private Boolean isControlledSubstance = false;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String unit;

    @Column(precision = 10, scale = 2)
    private BigDecimal packSize = BigDecimal.ONE;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(length = 50)
    private String hsnCode;

    @Column(nullable = false)
    private Integer currentStock = 0;

    @Column(nullable = false)
    private Integer reorderLevel = 10;

    @Column(nullable = false)
    private Integer reorderQuantity = 50;

    @Column(nullable = false)
    private Integer minimumStock = 5;

    @Column(nullable = false)
    private Integer maximumStock = 500;

    @Column(length = 50)
    private String rackNumber;

    @Column(length = 50)
    private String shelfNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_vendor_id")
    private Vendor preferredVendor;

    private String storageConditions;

    private Boolean isRefrigerated = false;

    @Column(length = 500)
    private String notes;

    private Boolean isActive = true;

    public boolean isLowStock() {
        return currentStock <= reorderLevel;
    }

    public boolean isOutOfStock() {
        return currentStock <= 0;
    }
}
