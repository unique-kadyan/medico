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
public class PatientConsentDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long sourceHospitalId;
    private String sourceHospitalName;
    private Long targetHospitalId;
    private String targetHospitalName;
    private ConsentStatus status;
    private SharingScope sharingScope;
    private String purpose;
    private LocalDateTime consentGrantedAt;
    private LocalDateTime consentRevokedAt;
    private LocalDateTime expiresAt;
    private String revokeReason;
    private String patientVerificationMethod;
    private LocalDateTime patientVerifiedAt;
    private String specificRecordIds;
    private Long requestedById;
    private String requestedByName;
    private Long approvedById;
    private String approvedByName;
    private String notes;
    private Integer accessCount;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isValid;
    private Boolean isExpired;
}
