package com.kaddy.repository;

import com.kaddy.model.EmergencyPatient;
import com.kaddy.model.enums.PatientCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyPatientRepository extends JpaRepository<EmergencyPatient, Long> {

    List<EmergencyPatient> findByPatientId(Long patientId);

    List<EmergencyPatient> findByEmergencyRoomId(Long roomId);

    List<EmergencyPatient> findByAttendingDoctorId(Long doctorId);

    List<EmergencyPatient> findByCondition(PatientCondition condition);

    @Query("SELECT e FROM EmergencyPatient e WHERE e.dischargeTime IS NULL ORDER BY e.triageLevel, e.admissionTime")
    List<EmergencyPatient> findCurrentPatients();

    @Query("SELECT e FROM EmergencyPatient e WHERE e.emergencyRoom.id = :roomId AND e.dischargeTime IS NULL")
    List<EmergencyPatient> findActivePatientsByRoom(@Param("roomId") Long roomId);

    @Query("SELECT e FROM EmergencyPatient e WHERE e.patient.id = :patientId AND e.dischargeTime IS NULL")
    Optional<EmergencyPatient> findActiveByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT e FROM EmergencyPatient e WHERE e.condition = :condition AND e.dischargeTime IS NULL")
    List<EmergencyPatient> findActivePatientsByCondition(@Param("condition") PatientCondition condition);

    @Query("SELECT e FROM EmergencyPatient e WHERE e.admissionTime BETWEEN :startDate AND :endDate ORDER BY e.admissionTime DESC")
    List<EmergencyPatient> findByAdmissionTimeBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM EmergencyPatient e WHERE e.requiresMonitoring = true AND e.dischargeTime IS NULL ORDER BY e.triageLevel")
    List<EmergencyPatient> findPatientsRequiringMonitoring();
}
