package com.kaddy.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalSummaryRequest {
    private Long patientId;
    private String diagnosis;
    private List<String> symptoms;
    private String treatmentProvided;
    private List<String> medicationsPrescribed;
    private String labResults;
    private String vitalSigns;
}
