package com.kaddy.dto.consent;

import com.kaddy.model.enums.SharingScope;
import jakarta.validation.constraints.NotBlank;
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
public class CreateShareRequestDTO {

    @NotBlank(message = "Patient identifier is required")
    private String patientIdentifier;

    private String patientFirstName;
    private String patientLastName;
    private String patientDateOfBirth;
    private String patientPhone;
    private String patientEmail;

    @NotNull(message = "Source hospital ID is required")
    private Long sourceHospitalId;

    @NotNull(message = "Requesting doctor ID is required")
    private Long requestingDoctorId;

    @NotNull(message = "Requested scope is required")
    private SharingScope requestedScope;

    @NotBlank(message = "Clinical purpose is required")
    private String clinicalPurpose;

    private String urgencyLevel;

    private LocalDateTime validUntil;

    private String additionalNotes;
}
