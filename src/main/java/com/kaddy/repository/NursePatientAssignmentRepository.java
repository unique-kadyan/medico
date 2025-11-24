package com.kaddy.repository;

import com.kaddy.model.NursePatientAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NursePatientAssignmentRepository extends JpaRepository<NursePatientAssignment, Long> {

    List<NursePatientAssignment> findByNurseId(Long nurseId);

    List<NursePatientAssignment> findByPatientId(Long patientId);

    List<NursePatientAssignment> findByNurseIdAndActive(Long nurseId, boolean active);

    List<NursePatientAssignment> findByPatientIdAndActive(Long patientId, boolean active);

    @Query("SELECT COUNT(n) FROM NursePatientAssignment n WHERE n.nurse.id = :nurseId AND n.active = true")
    long countActiveAssignmentsByNurse(@Param("nurseId") Long nurseId);

    @Query("SELECT n FROM NursePatientAssignment n WHERE n.nurse.id = :nurseId AND n.patient.id = :patientId")
    List<NursePatientAssignment> findByNurseIdAndPatientId(@Param("nurseId") Long nurseId,
            @Param("patientId") Long patientId);
}
