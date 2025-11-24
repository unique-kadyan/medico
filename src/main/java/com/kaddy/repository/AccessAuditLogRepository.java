package com.kaddy.repository;

import com.kaddy.model.AccessAuditLog;
import com.kaddy.model.enums.AuditActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessAuditLogRepository extends JpaRepository<AccessAuditLog, Long> {

    Page<AccessAuditLog> findByPatientId(Long patientId, Pageable pageable);

    List<AccessAuditLog> findByPatientIdOrderByActionTimestampDesc(Long patientId);

    Page<AccessAuditLog> findByPerformedById(Long userId, Pageable pageable);

    Page<AccessAuditLog> findByActionType(AuditActionType actionType, Pageable pageable);

    Page<AccessAuditLog> findByHospitalId(Long hospitalId, Pageable pageable);

    List<AccessAuditLog> findByConsentId(Long consentId);

    List<AccessAuditLog> findByShareRequestId(Long shareRequestId);

    @Query("SELECT a FROM AccessAuditLog a WHERE a.actionTimestamp BETWEEN :startDate AND :endDate "
            + "ORDER BY a.actionTimestamp DESC")
    Page<AccessAuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT a FROM AccessAuditLog a WHERE a.patient.id = :patientId "
            + "AND a.actionType IN ('RECORD_ACCESSED', 'RECORD_SHARED', 'RECORD_EXPORTED') "
            + "ORDER BY a.actionTimestamp DESC")
    List<AccessAuditLog> findPatientAccessHistory(@Param("patientId") Long patientId);

    @Query("SELECT a FROM AccessAuditLog a WHERE a.patient.id = :patientId "
            + "AND a.actionType IN ('CONSENT_REQUESTED', 'CONSENT_GRANTED', 'CONSENT_DENIED', 'CONSENT_REVOKED') "
            + "ORDER BY a.actionTimestamp DESC")
    List<AccessAuditLog> findConsentAuditTrail(@Param("patientId") Long patientId);

    @Query("SELECT a FROM AccessAuditLog a WHERE "
            + "(a.hospital.id = :hospitalId OR a.targetHospital.id = :hospitalId) "
            + "AND a.actionType IN ('SHARE_REQUEST_CREATED', 'SHARE_REQUEST_APPROVED', 'SHARE_REQUEST_DENIED', 'RECORD_SHARED') "
            + "ORDER BY a.actionTimestamp DESC")
    Page<AccessAuditLog> findHospitalSharingActivity(@Param("hospitalId") Long hospitalId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM AccessAuditLog a WHERE a.patient.id = :patientId "
            + "AND a.actionTimestamp BETWEEN :startDate AND :endDate")
    long countPatientAccessEvents(@Param("patientId") Long patientId, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM AccessAuditLog a WHERE a.success = false " + "ORDER BY a.actionTimestamp DESC")
    Page<AccessAuditLog> findFailedAttempts(Pageable pageable);

    List<AccessAuditLog> findByIpAddressOrderByActionTimestampDesc(String ipAddress);
}
