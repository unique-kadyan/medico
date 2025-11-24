package com.kaddy.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAnalysisRequest {
    private List<String> symptoms;
    private String duration;
    private String severity;
    private Integer patientAge;
    private String patientGender;
    private List<String> medicalHistory;
    private List<String> currentMedications;
}
