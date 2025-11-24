package com.kaddy.dto.consent;

import com.kaddy.model.enums.ConsentStatus;
import com.kaddy.model.enums.SharingScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordShareRequestDTO {
    private Long id;
    private String requestNumber;
    private Long patientId;
    private String patientName;
    private Long requestingHospitalId;
    private String requestingHospitalName;
    private Long sourceHospitalId;
    private String sourceHospitalName;
    private Long requestingDoctorId;
    private String requestingDoctorName;
    private ConsentStatus status;
    private SharingScope requestedScope;
    private String clinicalPurpose;
    private String urgencyLevel;
    private String patientIdentifier;
    private String patientDateOfBirth;
    private String patientPhone;
    private String patientEmail;
    private Long consentId;
    private LocalDateTime respondedAt;
    private Long respondedById;
    private String respondedByName;
    private String responseNotes;
    private LocalDateTime dataSharedAt;
    private String shareMethod;
    private String sharedRecordIds;
    private LocalDateTime expiresAt;
    private LocalDateTime validUntil;
    private String additionalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPending;
    private Boolean isApproved;
    private Boolean isExpired;
    private Boolean isAccessValid;
}
