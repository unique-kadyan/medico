package com.kaddy.dto;

import com.kaddy.model.enums.MedicationRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequestDTO {
    private Long id;

    @NotNull(message = "Requested by user ID is required")
    private Long requestedById;

    @NotBlank(message = "Medication name is required")
    private String medicationName;

    private String description;

    private String manufacturer;

    private String dosageForm;

    private String strength;

    private String requestReason;

    private BigDecimal estimatedCost;

    private LocalDateTime requestDate;

    private Long reviewedById;

    private LocalDateTime reviewDate;

    private String reviewNotes;

    @NotNull(message = "Status is required")
    private MedicationRequestStatus status;

    // Additional fields for display
    private String requestedByName;
    private String reviewedByName;
}
