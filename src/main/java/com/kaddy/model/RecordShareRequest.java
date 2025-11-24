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
@Table(name = "record_share_requests")
public class RecordShareRequest extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String requestNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_hospital_id", nullable = false)
    private Hospital requestingHospital;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_hospital_id", nullable = false)
    private Hospital sourceHospital;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_doctor_id", nullable = false)
    private Doctor requestingDoctor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentStatus status = ConsentStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharingScope requestedScope = SharingScope.SUMMARY_ONLY;

    @Column(length = 1000, nullable = false)
    private String clinicalPurpose;

    @Column(length = 500)
    private String urgencyLevel;

    @Column(length = 100)
    private String patientIdentifier;

    @Column(length = 100)
    private String patientDateOfBirth;

    @Column(length = 100)
    private String patientPhone;

    @Column(length = 200)
    private String patientEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id")
    private PatientConsent consent;

    @Column
    private LocalDateTime respondedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by_id")
    private User respondedBy;

    @Column(length = 1000)
    private String responseNotes;

    @Column
    private LocalDateTime dataSharedAt;

    @Column(length = 500)
    private String shareMethod;

    @Column(columnDefinition = "TEXT")
    private String sharedRecordIds;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime validUntil;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    public boolean isPending() {
        return status == ConsentStatus.PENDING;
    }

    public boolean isApproved() {
        return status == ConsentStatus.APPROVED;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isAccessValid() {
        return isApproved() && (validUntil == null || LocalDateTime.now().isBefore(validUntil));
    }
}
