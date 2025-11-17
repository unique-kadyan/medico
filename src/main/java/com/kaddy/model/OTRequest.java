package com.kaddy.model;

import com.kaddy.model.enums.OTRequestStatus;
import com.kaddy.model.enums.SurgeryType;
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
@Table(name = "ot_requests")
public class OTRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surgeon_id", nullable = false)
    @NotNull(message = "Surgeon is required")
    private Doctor surgeon;

    @NotNull(message = "Surgery type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurgeryType surgeryType;

    @NotNull(message = "Scheduled start time is required")
    @Column(nullable = false)
    private LocalDateTime scheduledStartTime;

    @Column
    private LocalDateTime scheduledEndTime;

    @Column
    private Integer estimatedDurationMinutes;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotNull(message = "Surgery purpose is required")
    private String surgeryPurpose;

    @Column(columnDefinition = "TEXT")
    private String surgeryNotes;

    @Column(columnDefinition = "TEXT")
    private String preOperativeInstructions;

    @Column(columnDefinition = "TEXT")
    private String requiredInstruments;

    @Column(columnDefinition = "TEXT")
    private String requiredMedications;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OTRequestStatus status = OTRequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column
    private LocalDateTime approvedAt;

    @Column(columnDefinition = "TEXT")
    private String approvalNotes;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column
    private LocalDateTime actualStartTime;

    @Column
    private LocalDateTime actualEndTime;

    @Column(columnDefinition = "TEXT")
    private String postOperativeNotes;

    @Column
    private String otRoomNumber;

    @Column
    private Boolean isEmergency = false;
}
