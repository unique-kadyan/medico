package com.kaddy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NursePatientAssignmentDTO {

    private Long id;

    @NotNull(message = "Nurse ID is required")
    private Long nurseId;

    private String nurseFirstName;
    private String nurseLastName;
    private String nurseEmail;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private String patientFirstName;
    private String patientLastName;
    private String patientIdentifier;

    private String assignedAs = "PRIMARY";
    private String notes;
    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
