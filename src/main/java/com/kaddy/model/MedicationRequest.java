package com.kaddy.model;

import com.kaddy.model.enums.MedicationRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "medication_requests")
public class MedicationRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    @NotNull(message = "Requested by user is required")
    private User requestedBy;

    @NotBlank(message = "Medication name is required")
    @Column(name = "medication_name", nullable = false, length = 255)
    private String medicationName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Column(name = "dosage_form", length = 100)
    private String dosageForm;

    @Column(name = "strength", length = 100)
    private String strength;

    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MedicationRequestStatus status = MedicationRequestStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
        if (status == null) {
            status = MedicationRequestStatus.PENDING;
        }
    }
}
