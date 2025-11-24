package com.kaddy.dto.consent;

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
public class SharedMedicalRecordDTO {

    private String shareRequestNumber;
    private Long consentId;
    private String sharingScope;
    private LocalDateTime sharedAt;
    private LocalDateTime validUntil;
    private String sourceHospitalName;
    private String sourceHospitalCode;

    private PatientSummary patient;

    private List<MedicalHistoryItem> medicalHistory;
    private List<LabResultItem> labResults;
    private List<PrescriptionItem> prescriptions;
    private List<DiagnosisItem> diagnoses;
    private List<VitalSignItem> vitalSigns;
    private List<AllergyItem> allergies;
    private String clinicalSummary;

    private String fhirBundle;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientSummary {
        private String patientId;
        private String firstName;
        private String lastName;
        private String dateOfBirth;
        private String gender;
        private String bloodGroup;
        private String phone;
        private String email;
        private String address;
        private String emergencyContact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalHistoryItem {
        private Long id;
        private LocalDateTime recordDate;
        private String chiefComplaint;
        private String diagnosis;
        private String treatment;
        private String doctorName;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabResultItem {
        private Long id;
        private String testName;
        private String testCode;
        private LocalDateTime testDate;
        private String result;
        private String normalRange;
        private String status;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionItem {
        private Long id;
        private LocalDateTime prescribedDate;
        private String medicationName;
        private String dosage;
        private String frequency;
        private String duration;
        private String prescribedBy;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisItem {
        private Long id;
        private LocalDateTime diagnosisDate;
        private String diagnosisCode;
        private String diagnosisName;
        private String severity;
        private String status;
        private String diagnosedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VitalSignItem {
        private Long id;
        private LocalDateTime recordedAt;
        private String bloodPressure;
        private String heartRate;
        private String temperature;
        private String respiratoryRate;
        private String oxygenSaturation;
        private String weight;
        private String height;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllergyItem {
        private Long id;
        private String allergen;
        private String reaction;
        private String severity;
        private LocalDateTime recordedAt;
    }
}
