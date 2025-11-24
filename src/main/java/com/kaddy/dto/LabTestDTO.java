package com.kaddy.dto;

import com.kaddy.model.LabTest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabTestDTO {
    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    private Long labTechnicianId;

    @NotBlank(message = "Test name is required")
    private String testName;

    private String testType;

    @NotNull(message = "Ordered date is required")
    private LocalDateTime orderedDate;

    private LocalDateTime sampleCollectedDate;

    private LocalDateTime resultDate;

    private String testResults;

    private String resultFilePath;

    private String normalRange;

    private String unit;

    private String remarks;

    @NotNull(message = "Status is required")
    private LabTest.TestStatus status;

    private LabTest.TestPriority priority;

    private Boolean urgent;

    private String patientName;
    private String doctorName;
    private String labTechnicianName;
}
