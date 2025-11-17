package com.kaddy.repository;

import com.kaddy.model.DoctorPatientAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorPatientAssignmentRepository extends JpaRepository<DoctorPatientAssignment, Long> {

    @Query("SELECT a FROM DoctorPatientAssignment a WHERE a.doctor.id = :doctorId AND a.status = 'ACTIVE'")
    List<DoctorPatientAssignment> findActiveAssignmentsByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT a FROM DoctorPatientAssignment a WHERE a.patient.id = :patientId AND a.status = 'ACTIVE'")
    List<DoctorPatientAssignment> findActiveAssignmentsByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT a FROM DoctorPatientAssignment a WHERE a.doctor.id = :doctorId AND a.patient.id = :patientId AND a.status = 'ACTIVE'")
    Optional<DoctorPatientAssignment> findActiveAssignment(@Param("doctorId") Long doctorId, @Param("patientId") Long patientId);

    @Query("SELECT DISTINCT a.patient.id FROM DoctorPatientAssignment a WHERE a.doctor.id = :doctorId AND a.status = 'ACTIVE'")
    List<Long> findPatientIdsByDoctorId(@Param("doctorId") Long doctorId);
}
