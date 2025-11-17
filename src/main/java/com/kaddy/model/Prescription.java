package com.kaddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prescriptions")
public class Prescription extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String prescriptionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctor is required")
    private Doctor doctor;

    @NotNull(message = "Prescription date is required")
    @Column(nullable = false)
    private LocalDate prescriptionDate = LocalDate.now();

    @Column
    private LocalDate expiryDate;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false)
    private Boolean dispensed = false;

    @Column
    private LocalDate dispensedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensed_by")
    private User dispensedBy;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> items = new ArrayList<>();

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}
