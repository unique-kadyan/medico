package com.kaddy.dto;

import com.kaddy.model.enums.OTRequestStatus;
import com.kaddy.model.enums.SurgeryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTRequestDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long surgeonId;
    private String surgeonName;
    private SurgeryType surgeryType;
    private String surgeryPurpose;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private Integer estimatedDurationMinutes;
    private String otRoomNumber;
    private String requiredInstruments;
    private String requiredMedications;
    private Boolean isEmergency;
    private OTRequestStatus status;
    private String notes;
    private LocalDateTime approvedAt;
    private Long approvedById;
    private String approvedByName;
    private String rejectionReason;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String postOperativeNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
