package com.kaddy.repository;

import com.kaddy.model.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {

    List<LabTest> findByPatientId(Long patientId);

    List<LabTest> findByDoctorId(Long doctorId);

    List<LabTest> findByStatus(LabTest.TestStatus status);

    List<LabTest> findByPatientIdOrderByOrderedDateDesc(Long patientId);
}
