package com.kaddy.model;

import com.kaddy.model.enums.InsuranceClaimStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_claims", indexes = {
        @Index(name = "idx_claim_number", columnList = "claimNumber", unique = true),
        @Index(name = "idx_claim_hospital", columnList = "hospital_id"),
        @Index(name = "idx_claim_patient", columnList = "patient_id"),
        @Index(name = "idx_claim_invoice", columnList = "invoice_id"),
        @Index(name = "idx_claim_status", columnList = "status"),
        @Index(name = "idx_claim_provider", columnList = "insurance_provider_id")})
public class InsuranceClaim extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_insurance_id", nullable = false)
    private PatientInsurance patientInsurance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_provider_id", nullable = false)
    private InsuranceProvider insuranceProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id")
    private PatientAdmission admission;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsuranceClaimStatus status = InsuranceClaimStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate claimDate;

    private LocalDate submissionDate;
    private LocalDate responseDate;
    private LocalDate settlementDate;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal claimedAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal approvedAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal rejectedAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal settledAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal deductionAmount = BigDecimal.ZERO;

    private String preAuthNumber;
    private LocalDate preAuthDate;
    private LocalDate preAuthExpiryDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal preAuthAmount;

    @Column(length = 100)
    private String insuranceReferenceNumber;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 500)
    private String deductionReason;

    private String claimDocumentUrl;
    private String supportingDocumentsUrl;

    @Column(length = 500)
    private String diagnosisCodes;

    @Column(length = 500)
    private String procedureCodes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    private LocalDateTime processedAt;

    @Column(length = 1000)
    private String notes;

    private Boolean isAppealed = false;

    @Column(length = 500)
    private String appealReason;

    private LocalDate appealDate;

    private String appealReferenceNumber;

    @Enumerated(EnumType.STRING)
    private InsuranceClaimStatus appealStatus;
}
