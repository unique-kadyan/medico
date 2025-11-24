package com.kaddy.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAnalysisResponse {
    private List<String> symptoms;
    private List<PossibleCondition> possibleConditions;
    private String urgencyLevel;
    private String recommendedSpecialist;
    private List<String> immediateActions;
    private List<String> warningSignsToWatch;
    private String aiResponse;
    private String disclaimer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PossibleCondition {
        private String name;
        private String probability;
        private String description;
    }
}
