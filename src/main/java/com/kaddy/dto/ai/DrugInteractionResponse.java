package com.kaddy.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugInteractionResponse {
    private List<String> medications;
    private boolean hasInteractions;
    private List<Interaction> interactions;
    private String overallRisk;
    private String summary;
    private List<String> generalRecommendations;
    private String aiResponse;
    private String disclaimer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Interaction {
        private String drug1;
        private String drug2;
        private String severity;
        private String description;
        private String recommendation;
    }
}
