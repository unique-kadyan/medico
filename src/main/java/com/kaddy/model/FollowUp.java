package com.kaddy.model;

import jakarta.persistence.*;
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
@Table(name = "follow_ups")
public class FollowUp extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "followup_date", nullable = false)
    private LocalDateTime followupDate;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "prescription", columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "vital_signs", columnDefinition = "TEXT")
    private String vitalSigns;

    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    @Column(name = "next_followup_date")
    private LocalDateTime nextFollowupDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FollowUpStatus status = FollowUpStatus.SCHEDULED;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    public enum FollowUpStatus {
        SCHEDULED,
        COMPLETED,
        CANCELLED,
        NO_SHOW,
        RESCHEDULED
    }
}
