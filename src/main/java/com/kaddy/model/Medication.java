package com.kaddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "medications")
public class Medication extends BaseEntity {

    @NotBlank(message = "Medication code is required")
    @Column(unique = true, nullable = false)
    private String medicationCode;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @Column
    private String genericName;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category; // e.g., Antibiotic, Painkiller, Antacid, etc.

    @Column
    private String manufacturer;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Dosage form is required")
    @Column(nullable = false)
    private String dosageForm; // e.g., Tablet, Capsule, Syrup, Injection

    @Column
    private String strength; // e.g., 500mg, 10ml

    @NotNull(message = "Unit price is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "Stock quantity is required")
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column
    private Integer reorderLevel = 10; // Minimum stock level before reorder

    @Column
    private Integer reorderQuantity = 50; // Quantity to order when stock is low

    @Column
    private LocalDate expiryDate;

    @Column
    private String batchNumber;

    @Column
    private Boolean requiresPrescription = true;

    @Column(columnDefinition = "TEXT")
    private String sideEffects;

    @Column(columnDefinition = "TEXT")
    private String contraindications;

    @Column(columnDefinition = "TEXT")
    private String storageInstructions;

    public boolean isLowStock() {
        return stockQuantity <= reorderLevel;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now().plusMonths(3));
    }
}
