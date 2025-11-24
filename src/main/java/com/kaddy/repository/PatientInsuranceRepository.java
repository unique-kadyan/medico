package com.kaddy.repository;

import com.kaddy.model.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, Long> {

    List<PatientInsurance> findByPatientId(Long patientId);

    @Query("SELECT pi FROM PatientInsurance pi WHERE pi.patient.id = :patientId "
            + "AND pi.effectiveDate <= :today AND (pi.expirationDate IS NULL OR pi.expirationDate >= :today)")
    List<PatientInsurance> findActiveInsuranceByPatient(@Param("patientId") Long patientId,
            @Param("today") LocalDate today);

    @Query("SELECT pi FROM PatientInsurance pi WHERE pi.patient.id = :patientId AND pi.isPrimary = true "
            + "AND pi.effectiveDate <= :today AND (pi.expirationDate IS NULL OR pi.expirationDate >= :today)")
    Optional<PatientInsurance> findPrimaryInsurance(@Param("patientId") Long patientId,
            @Param("today") LocalDate today);

    Optional<PatientInsurance> findByPolicyNumber(String policyNumber);

    List<PatientInsurance> findByProviderId(Long providerId);

    boolean existsByPolicyNumber(String policyNumber);
}
