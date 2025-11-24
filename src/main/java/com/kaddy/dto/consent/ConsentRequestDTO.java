package com.kaddy.dto.consent;

import com.kaddy.model.enums.SharingScope;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequestDTO {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Target hospital ID is required")
    private Long targetHospitalId;

    @NotNull(message = "Sharing scope is required")
    private SharingScope sharingScope;

    @NotNull(message = "Purpose is required")
    private String purpose;

    private LocalDateTime expiresAt;

    private List<Long> specificRecordIds;

    private String notes;

    private String patientEmail;
    private String patientPhone;
}
