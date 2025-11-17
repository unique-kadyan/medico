package com.kaddy.model;

import com.kaddy.model.enums.PatientCondition;
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
@Table(name = "emergency_patients")
public class EmergencyPatient extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_room_id", nullable = false)
    @NotNull(message = "Emergency room is required")
    private EmergencyRoom emergencyRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attending_doctor_id")
    private Doctor attendingDoctor;

    @NotNull(message = "Admission time is required")
    @Column(nullable = false)
    private LocalDateTime admissionTime;

    @Column
    private LocalDateTime dischargeTime;

    @NotNull(message = "Chief complaint is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    private String initialAssessment;

    @Column(columnDefinition = "TEXT")
    private String vitalSigns;

    @Column(columnDefinition = "TEXT")
    private String treatmentPlan;

    @Column(columnDefinition = "TEXT")
    private String progressNotes;

    @NotNull(message = "Patient condition is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientCondition condition = PatientCondition.STABLE;

    @Column
    private Integer triageLevel; // 1-5, with 1 being most urgent

    @Column
    private Boolean requiresMonitoring = false;

    @Column
    private Boolean isAdmittedToHospital = false;

    @Column(columnDefinition = "TEXT")
    private String dischargeNotes;
}
