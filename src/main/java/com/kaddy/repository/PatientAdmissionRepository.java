package com.kaddy.repository;

import com.kaddy.model.PatientAdmission;
import com.kaddy.model.enums.AdmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientAdmissionRepository extends JpaRepository<PatientAdmission, Long> {

    Optional<PatientAdmission> findByAdmissionNumber(String admissionNumber);

    List<PatientAdmission> findByHospitalIdAndStatusOrderByAdmissionDateTimeDesc(Long hospitalId,
            AdmissionStatus status);

    List<PatientAdmission> findByPatientIdOrderByAdmissionDateTimeDesc(Long patientId);

    List<PatientAdmission> findByHospitalIdAndPatientIdOrderByAdmissionDateTimeDesc(Long hospitalId, Long patientId);

    @Query("SELECT pa FROM PatientAdmission pa WHERE pa.hospital.id = :hospitalId AND pa.status = 'ADMITTED' ORDER BY pa.admissionDateTime DESC")
    List<PatientAdmission> findCurrentAdmissions(@Param("hospitalId") Long hospitalId);

    @Query("SELECT pa FROM PatientAdmission pa WHERE pa.bed.id = :bedId AND pa.status = 'ADMITTED'")
    Optional<PatientAdmission> findCurrentAdmissionByBed(@Param("bedId") Long bedId);

    @Query("SELECT pa FROM PatientAdmission pa WHERE pa.patient.id = :patientId AND pa.status = 'ADMITTED'")
    Optional<PatientAdmission> findCurrentAdmissionByPatient(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(pa) FROM PatientAdmission pa WHERE pa.hospital.id = :hospitalId AND pa.status = 'ADMITTED'")
    long countCurrentAdmissions(@Param("hospitalId") Long hospitalId);

    @Query("SELECT pa FROM PatientAdmission pa WHERE pa.hospital.id = :hospitalId AND pa.admissionDateTime BETWEEN :startDate AND :endDate ORDER BY pa.admissionDateTime DESC")
    List<PatientAdmission> findByHospitalIdAndDateRange(@Param("hospitalId") Long hospitalId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pa FROM PatientAdmission pa WHERE pa.ward.id = :wardId AND pa.status = 'ADMITTED' ORDER BY pa.bed.bedNumber")
    List<PatientAdmission> findCurrentAdmissionsByWard(@Param("wardId") Long wardId);

    @Query("SELECT pa FROM PatientAdmission pa WHERE pa.attendingDoctor.id = :doctorId AND pa.status = 'ADMITTED' ORDER BY pa.admissionDateTime DESC")
    List<PatientAdmission> findCurrentAdmissionsByDoctor(@Param("doctorId") Long doctorId);

    boolean existsByPatientIdAndStatus(Long patientId, AdmissionStatus status);
}
