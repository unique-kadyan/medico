package com.kaddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_patient_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DoctorPatientAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate;

    @Column(name = "primary_doctor")
    private Boolean primaryDoctor = false;

    @Column(name = "notes")
    private String notes;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

    public enum AssignmentStatus {
        ACTIVE, COMPLETED, TRANSFERRED
    }
}
