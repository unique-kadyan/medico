package com.kaddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nurse_patient_assignments")
public class NursePatientAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id", nullable = false)
    @NotNull(message = "Nurse is required")
    private User nurse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @Column(nullable = false)
    private String assignedAs = "PRIMARY";

    @Column(columnDefinition = "TEXT")
    private String notes;

    private boolean active = true;
}
