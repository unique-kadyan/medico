package com.kaddy.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmartSchedulingRequest {
    private String patientCondition;
    private String urgencyLevel;
    private String preferredTimeOfDay;
    private List<LocalDate> preferredDates;
    private Long preferredDoctorId;
    private String specialization;
    private Long patientId;
}
