package com.kaddy.dto;

import com.kaddy.model.enums.PatientCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyPatientDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long emergencyRoomId;
    private String emergencyRoomNumber;
    private Long attendingDoctorId;
    private String attendingDoctorName;
    private PatientCondition condition;
    private Integer triageLevel;
    private LocalDateTime admissionTime;
    private LocalDateTime dischargeTime;
    private String chiefComplaint;
    private String vitalSigns;
    private String treatmentPlan;
    private String medicationsAdministered;
    private Boolean requiresMonitoring;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
