package com.kaddy.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalSummaryResponse {
    private String aiGeneratedSummary;
    private List<String> keyFindings;
    private String treatmentPlan;
    private List<String> followUpRecommendations;
    private List<String> patientInstructions;
    private LocalDateTime generatedAt;
}
