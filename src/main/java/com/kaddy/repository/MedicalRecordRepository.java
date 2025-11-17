package com.kaddy.repository;

import com.kaddy.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientId(Long patientId);

    List<MedicalRecord> findByDoctorId(Long doctorId);

    List<MedicalRecord> findByRecordType(String recordType);

    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId ORDER BY m.recordDate DESC")
    List<MedicalRecord> findPatientRecordsOrderedByDate(Long patientId);
}
