package com.kaddy.model;

import com.kaddy.model.enums.ConsentStatus;
import com.kaddy.model.enums.SharingScope;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patient_consents")
public class PatientConsent extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_hospital_id", nullable = false)
    private Hospital sourceHospital;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_hospital_id", nullable = false)
    private Hospital targetHospital;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentStatus status = ConsentStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharingScope sharingScope = SharingScope.SUMMARY_ONLY;

    @Column(length = 1000)
    private String purpose;

    @Column
    private LocalDateTime consentGrantedAt;

    @Column
    private LocalDateTime consentRevokedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(length = 500)
    private String revokeReason;

    @Column(length = 500)
    private String consentSignature;

    @Column
    private String patientVerificationMethod;

    @Column
    private LocalDateTime patientVerifiedAt;

    @Column(columnDefinition = "TEXT")
    private String specificRecordIds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id")
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(length = 1000)
    private String notes;

    @Column
    private Integer accessCount = 0;

    @Column
    private LocalDateTime lastAccessedAt;

    public boolean isValid() {
        if (status != ConsentStatus.APPROVED) {
            return false;
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementAccessCount() {
        this.accessCount = (this.accessCount == null ? 0 : this.accessCount) + 1;
        this.lastAccessedAt = LocalDateTime.now();
    }
}
