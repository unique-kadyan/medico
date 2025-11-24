package com.kaddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patient_insurance", indexes = {@Index(name = "idx_patient_insurance_patient", columnList = "patient_id"),
        @Index(name = "idx_patient_insurance_policy", columnList = "policyNumber"),
        @Index(name = "idx_patient_insurance_provider", columnList = "provider_id")})
public class PatientInsurance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private InsuranceProvider provider;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String policyNumber;

    @Column(length = 100)
    private String groupNumber;

    @Column(length = 100)
    private String memberId;

    private String planName;
    private String planType;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal annualLimit;

    @Column(precision = 12, scale = 2)
    private BigDecimal remainingLimit;

    @Column(precision = 12, scale = 2)
    private BigDecimal deductible;

    @Column(precision = 12, scale = 2)
    private BigDecimal deductibleMet = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal coPayPercentage;

    @Column(precision = 12, scale = 2)
    private BigDecimal coPayAmount;

    private Boolean isPrimary = true;

    private String policyHolderName;
    private String policyHolderRelation;
    private LocalDate policyHolderDob;

    private Boolean requiresPreAuth = false;
    private String preAuthPhone;

    private String insuranceCardFrontUrl;
    private String insuranceCardBackUrl;

    private Boolean isVerified = false;
    private LocalDate lastVerifiedDate;

    @Column(length = 500)
    private String notes;

    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return effectiveDate.isBefore(today) || effectiveDate.isEqual(today)
                && (expirationDate == null || expirationDate.isAfter(today) || expirationDate.isEqual(today));
    }
}
