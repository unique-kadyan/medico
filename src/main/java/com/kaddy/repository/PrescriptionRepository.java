package com.kaddy.repository;

import com.kaddy.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    List<Prescription> findByPatientId(Long patientId);

    List<Prescription> findByDoctorId(Long doctorId);

    @Query("SELECT p FROM Prescription p WHERE p.dispensed = false AND p.active = true")
    List<Prescription> findUndispensedPrescriptions();

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND p.dispensed = false")
    List<Prescription> findUndispensedPrescriptionsByPatient(Long patientId);

    boolean existsByPrescriptionNumber(String prescriptionNumber);
}
