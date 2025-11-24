package com.kaddy.model;

import com.kaddy.model.enums.AuditActionType;
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
@Table(name = "access_audit_logs", indexes = {@Index(name = "idx_audit_patient", columnList = "patient_id"),
        @Index(name = "idx_audit_action", columnList = "action_type"),
        @Index(name = "idx_audit_timestamp", columnList = "action_timestamp"),
        @Index(name = "idx_audit_user", columnList = "performed_by_id")})
public class AccessAuditLog extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActionType actionType;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime actionTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private User performedBy;

    @Column(length = 200)
    private String performedByName;

    @Column(length = 100)
    private String performedByRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(length = 200)
    private String patientName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_hospital_id")
    private Hospital targetHospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id")
    private PatientConsent consent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_request_id")
    private RecordShareRequest shareRequest;

    @Column(length = 100)
    private String resourceType;

    @Column
    private Long resourceId;

    @Column(columnDefinition = "TEXT")
    private String resourceIds;

    @Column(columnDefinition = "TEXT")
    private String actionDescription;

    @Column(length = 500)
    private String actionReason;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 100)
    private String sessionId;

    @Column
    private Boolean success = true;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 50)
    private String dataClassification;

    @Column(length = 100)
    private String fhirResourceType;

    @Column(length = 500)
    private String fhirResourceId;

    public static AccessAuditLog create(AuditActionType actionType, User performedBy, Patient patient) {
        AccessAuditLog log = new AccessAuditLog();
        log.setActionType(actionType);
        log.setActionTimestamp(LocalDateTime.now());
        log.setPerformedBy(performedBy);
        if (performedBy != null) {
            log.setPerformedByName(performedBy.getFirstName() + " " + performedBy.getLastName());
            log.setPerformedByRole(performedBy.getRole().name());
        }
        log.setPatient(patient);
        if (patient != null) {
            log.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        }
        return log;
    }
}
