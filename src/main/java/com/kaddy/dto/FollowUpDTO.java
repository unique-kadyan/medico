package com.kaddy.dto;

import com.kaddy.model.FollowUp;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpDTO {
    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Follow-up date is required")
    private LocalDateTime followupDate;

    private LocalDateTime scheduledDate;

    private String reason;

    private String diagnosis;

    private String prescription;

    private String notes;

    private String vitalSigns;

    private String treatmentPlan;

    private LocalDateTime nextFollowupDate;

    @NotNull(message = "Status is required")
    private FollowUp.FollowUpStatus status;

    private Integer durationMinutes;

    private String patientName;
    private String doctorName;
}
