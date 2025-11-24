package com.kaddy.model;

import com.kaddy.model.enums.AdmissionStatus;
import com.kaddy.model.enums.AdmissionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patient_admissions")
public class PatientAdmission extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String admissionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id")
    private Bed bed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitting_doctor_id")
    private Doctor admittingDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attending_doctor_id")
    private Doctor attendingDoctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdmissionType admissionType = AdmissionType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdmissionStatus status = AdmissionStatus.ADMITTED;

    @Column(nullable = false)
    private LocalDateTime admissionDateTime;

    private LocalDateTime expectedDischargeDate;

    private LocalDateTime actualDischargeDateTime;

    @Column(length = 1000)
    private String chiefComplaint;

    @Column(length = 2000)
    private String admissionDiagnosis;

    @Column(length = 2000)
    private String dischargeDiagnosis;

    @Column(length = 2000)
    private String treatmentPlan;

    @Column(length = 500)
    private String allergies;

    private String bloodGroup;

    @Column(length = 1000)
    private String vitalSigns;

    @Column(length = 2000)
    private String medicalHistory;

    @Column(length = 1000)
    private String surgicalHistory;

    @Column(length = 500)
    private String currentMedications;

    @Column(length = 500)
    private String emergencyContactName;

    @Column(length = 20)
    private String emergencyContactPhone;

    @Column(length = 100)
    private String emergencyContactRelation;

    @Column(length = 100)
    private String insuranceProvider;

    @Column(length = 50)
    private String insurancePolicyNumber;

    @Column(precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @Column(length = 2000)
    private String dischargeNotes;

    @Column(length = 1000)
    private String dischargeMedications;

    @Column(length = 1000)
    private String followUpInstructions;

    private LocalDateTime nextFollowUpDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discharged_by_id")
    private User dischargedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitted_by_id")
    private User admittedBy;

    @Column(length = 500)
    private String specialInstructions;

    @Column(nullable = false)
    private Boolean isEmergency = false;

    @Column(nullable = false)
    private Boolean requiresIcu = false;

    @Column(nullable = false)
    private Boolean hasInsurance = false;
}
