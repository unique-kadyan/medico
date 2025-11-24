package com.kaddy.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmartSchedulingResponse {
    private String patientCondition;
    private String recommendedSpecialist;
    private String suggestedTimeframe;
    private Integer appointmentDuration;
    private String priority;
    private List<SuggestedSlot> suggestedSlots;
    private List<String> preparationInstructions;
    private String aiResponse;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedSlot {
        private LocalDateTime dateTime;
        private Long doctorId;
        private String doctorName;
        private String reason;
    }
}
