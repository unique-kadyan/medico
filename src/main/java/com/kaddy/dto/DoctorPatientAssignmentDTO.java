package com.kaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPatientAssignmentDTO {
    private Long id;
    private Long doctorId;
    private Long patientId;
    private String doctorName;
    private String patientName;
    private LocalDateTime assignedDate;
    private Boolean primaryDoctor;
    private String notes;
    private String status;
}
