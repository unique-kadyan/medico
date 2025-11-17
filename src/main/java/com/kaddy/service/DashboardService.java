package com.kaddy.service;

import com.kaddy.dto.DashboardStatsDTO;
import com.kaddy.repository.AppointmentRepository;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.MedicationRepository;
import com.kaddy.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicationRepository medicationRepository;

    public DashboardStatsDTO getDashboardStats() {
        Long totalPatients = patientRepository.count();
        Long totalDoctors = doctorRepository.count();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        Long todayAppointments = (long) appointmentRepository
                .findAppointmentsBetweenDates(startOfDay, endOfDay)
                .size();

        Long lowStockMedications = medicationRepository.findAll().stream()
                .filter(med -> med.getStockQuantity() < 10)
                .count();

        List<DashboardStatsDTO.MonthlyPatientCount> monthlyPatients = getMonthlyPatientCounts();

        List<DashboardStatsDTO.AppointmentTypeCount> appointmentsByType = getAppointmentTypeDistribution();

        return DashboardStatsDTO.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .todayAppointments(todayAppointments)
                .lowStockMedications(lowStockMedications)
                .monthlyPatients(monthlyPatients)
                .appointmentsByType(appointmentsByType)
                .build();
    }

    private List<DashboardStatsDTO.MonthlyPatientCount> getMonthlyPatientCounts() {
        List<DashboardStatsDTO.MonthlyPatientCount> monthlyCounts = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            long count = patientRepository.findAll().stream()
                    .filter(p -> p.getCreatedAt() != null
                            && !p.getCreatedAt().isBefore(startOfMonth)
                            && !p.getCreatedAt().isAfter(endOfMonth))
                    .count();

            String monthName = yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            monthlyCounts.add(DashboardStatsDTO.MonthlyPatientCount.builder()
                    .month(monthName)
                    .patients(count)
                    .build());
        }

        return monthlyCounts;
    }

    private List<DashboardStatsDTO.AppointmentTypeCount> getAppointmentTypeDistribution() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();

        List<String> appointmentReasons = appointmentRepository
                .findAppointmentsBetweenDates(thirtyDaysAgo, now)
                .stream()
                .map(appointment -> {
                    String reason = appointment.getReasonForVisit();
                    if (reason == null || reason.trim().isEmpty()) {
                        return "General Checkup";
                    }
                    reason = reason.toLowerCase();
                    if (reason.contains("checkup") || reason.contains("check-up")) {
                        return "Checkup";
                    } else if (reason.contains("emergency") || reason.contains("urgent")) {
                        return "Emergency";
                    } else if (reason.contains("follow") || reason.contains("followup")) {
                        return "Follow-up";
                    } else if (reason.contains("surgery") || reason.contains("operation")) {
                        return "Surgery";
                    } else {
                        return "Other";
                    }
                })
                .toList();

        Map<String, Long> typeCounts = appointmentReasons.stream()
                .collect(Collectors.groupingBy(type -> type, Collectors.counting()));

        return typeCounts.entrySet().stream()
                .map(entry -> DashboardStatsDTO.AppointmentTypeCount.builder()
                        .name(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
