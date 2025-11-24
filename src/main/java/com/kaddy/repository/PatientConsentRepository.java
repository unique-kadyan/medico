package com.kaddy.repository;

import com.kaddy.model.PatientConsent;
import com.kaddy.model.enums.ConsentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientConsentRepository extends JpaRepository<PatientConsent, Long> {

    List<PatientConsent> findByPatientId(Long patientId);

    Page<PatientConsent> findByPatientId(Long patientId, Pageable pageable);

    List<PatientConsent> findBySourceHospitalId(Long hospitalId);

    Page<PatientConsent> findBySourceHospitalId(Long hospitalId, Pageable pageable);

    List<PatientConsent> findByTargetHospitalId(Long hospitalId);

    Page<PatientConsent> findByTargetHospitalId(Long hospitalId, Pageable pageable);

    List<PatientConsent> findByStatus(ConsentStatus status);

    Page<PatientConsent> findBySourceHospitalIdAndStatus(Long hospitalId, ConsentStatus status, Pageable pageable);

    @Query("SELECT pc FROM PatientConsent pc WHERE pc.patient.id = :patientId "
            + "AND pc.sourceHospital.id = :sourceHospitalId " + "AND pc.targetHospital.id = :targetHospitalId "
            + "AND pc.status = :status")
    Optional<PatientConsent> findActiveConsent(@Param("patientId") Long patientId,
            @Param("sourceHospitalId") Long sourceHospitalId, @Param("targetHospitalId") Long targetHospitalId,
            @Param("status") ConsentStatus status);

    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM PatientConsent pc "
            + "WHERE pc.patient.id = :patientId " + "AND pc.sourceHospital.id = :sourceHospitalId "
            + "AND pc.targetHospital.id = :targetHospitalId " + "AND pc.status = 'APPROVED' "
            + "AND (pc.expiresAt IS NULL OR pc.expiresAt > :now)")
    boolean hasValidConsent(@Param("patientId") Long patientId, @Param("sourceHospitalId") Long sourceHospitalId,
            @Param("targetHospitalId") Long targetHospitalId, @Param("now") LocalDateTime now);

    @Query("SELECT pc FROM PatientConsent pc WHERE pc.status = 'APPROVED' "
            + "AND pc.expiresAt IS NOT NULL AND pc.expiresAt < :now")
    List<PatientConsent> findExpiredConsents(@Param("now") LocalDateTime now);

    @Query("SELECT pc FROM PatientConsent pc WHERE pc.patient.id = :patientId "
            + "AND (pc.sourceHospital.id = :hospitalId OR pc.targetHospital.id = :hospitalId)")
    List<PatientConsent> findByPatientAndHospital(@Param("patientId") Long patientId,
            @Param("hospitalId") Long hospitalId);

    @Query("SELECT COUNT(pc) FROM PatientConsent pc WHERE pc.sourceHospital.id = :hospitalId "
            + "AND pc.status = 'PENDING'")
    long countPendingConsents(@Param("hospitalId") Long hospitalId);

    List<PatientConsent> findByRequestedById(Long userId);
}
