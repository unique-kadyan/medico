package com.kaddy.dto;

import com.kaddy.model.enums.AdmissionStatus;
import com.kaddy.model.enums.AdmissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientAdmissionDTO {
    private Long id;
    private String admissionNumber;

    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientEmail;

    private Long hospitalId;
    private String hospitalName;

    private Long bedId;
    private String bedNumber;

    private Long wardId;
    private String wardName;

    private Long admittingDoctorId;
    private String admittingDoctorName;

    private Long attendingDoctorId;
    private String attendingDoctorName;

    private AdmissionType admissionType;
    private AdmissionStatus status;

    private LocalDateTime admissionDateTime;
    private LocalDateTime expectedDischargeDate;
    private LocalDateTime actualDischargeDateTime;

    private String chiefComplaint;
    private String admissionDiagnosis;
    private String dischargeDiagnosis;
    private String treatmentPlan;
    private String allergies;
    private String bloodGroup;
    private String vitalSigns;
    private String medicalHistory;
    private String surgicalHistory;
    private String currentMedications;

    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;

    private String insuranceProvider;
    private String insurancePolicyNumber;

    private BigDecimal estimatedCost;
    private BigDecimal depositAmount;

    private String dischargeNotes;
    private String dischargeMedications;
    private String followUpInstructions;
    private LocalDateTime nextFollowUpDate;

    private Long dischargedById;
    private String dischargedByName;

    private Long admittedById;
    private String admittedByName;

    private String specialInstructions;

    private Boolean isEmergency;
    private Boolean requiresIcu;
    private Boolean hasInsurance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}
