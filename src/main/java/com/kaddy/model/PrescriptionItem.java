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
@Table(name = "prescription_items")
public class PrescriptionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @NotNull(message = "Prescription is required")
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    @NotNull(message = "Medication is required")
    private Medication medication;

    @NotNull(message = "Quantity is required")
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String dosage; // e.g., "1 tablet", "5ml"

    @Column(nullable = false)
    private String frequency; // e.g., "Twice daily", "Three times a day"

    @Column(nullable = false)
    private Integer duration; // Duration in days

    @Column(columnDefinition = "TEXT")
    private String instructions; // e.g., "Take after meals"

    @Column(columnDefinition = "TEXT")
    private String warnings;
}
