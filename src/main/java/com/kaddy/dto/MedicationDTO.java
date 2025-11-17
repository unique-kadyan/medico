package com.kaddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDTO {
    private Long id;

    @NotBlank(message = "Medication code is required")
    private String medicationCode;

    @NotBlank(message = "Name is required")
    private String name;

    private String genericName;

    @NotBlank(message = "Category is required")
    private String category;

    private String manufacturer;
    private String description;

    @NotBlank(message = "Dosage form is required")
    private String dosageForm;

    private String strength;

    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    @NotNull(message = "Stock quantity is required")
    private Integer stockQuantity;

    private Integer reorderLevel;
    private Integer reorderQuantity;
    private LocalDate expiryDate;
    private String batchNumber;
    private Boolean requiresPrescription;
    private String sideEffects;
    private String contraindications;
    private String storageInstructions;
    private Boolean active;
    private Boolean lowStock;
    private Boolean expired;
    private Boolean expiringSoon;
}
