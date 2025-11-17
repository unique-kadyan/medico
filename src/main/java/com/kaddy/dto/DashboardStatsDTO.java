package com.kaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private Long totalPatients;
    private Long totalDoctors;
    private Long todayAppointments;
    private Long lowStockMedications;
    private List<MonthlyPatientCount> monthlyPatients;
    private List<AppointmentTypeCount> appointmentsByType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyPatientCount {
        private String month;
        private Long patients;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppointmentTypeCount {
        private String name;
        private Long value;
    }
}
