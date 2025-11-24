package com.kaddy.repository;

import com.kaddy.model.RecordShareRequest;
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
public interface RecordShareRequestRepository extends JpaRepository<RecordShareRequest, Long> {

    Optional<RecordShareRequest> findByRequestNumber(String requestNumber);

    List<RecordShareRequest> findByRequestingHospitalId(Long hospitalId);

    Page<RecordShareRequest> findByRequestingHospitalId(Long hospitalId, Pageable pageable);

    List<RecordShareRequest> findBySourceHospitalId(Long hospitalId);

    Page<RecordShareRequest> findBySourceHospitalId(Long hospitalId, Pageable pageable);

    List<RecordShareRequest> findByPatientId(Long patientId);

    Page<RecordShareRequest> findByPatientId(Long patientId, Pageable pageable);

    Page<RecordShareRequest> findByStatus(ConsentStatus status, Pageable pageable);

    @Query("SELECT r FROM RecordShareRequest r WHERE r.sourceHospital.id = :hospitalId "
            + "AND r.status = 'PENDING' ORDER BY r.createdAt DESC")
    List<RecordShareRequest> findPendingRequestsForHospital(@Param("hospitalId") Long hospitalId);

    List<RecordShareRequest> findByRequestingDoctorId(Long doctorId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RecordShareRequest r "
            + "WHERE r.patient.id = :patientId " + "AND r.requestingHospital.id = :requestingHospitalId "
            + "AND r.sourceHospital.id = :sourceHospitalId " + "AND r.status = 'PENDING'")
    boolean existsPendingRequest(@Param("patientId") Long patientId,
            @Param("requestingHospitalId") Long requestingHospitalId, @Param("sourceHospitalId") Long sourceHospitalId);

    @Query("SELECT r FROM RecordShareRequest r WHERE r.status = 'PENDING' "
            + "AND r.expiresAt IS NOT NULL AND r.expiresAt < :now")
    List<RecordShareRequest> findExpiredRequests(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM RecordShareRequest r WHERE r.sourceHospital.id = :hospitalId "
            + "AND r.status = 'PENDING'")
    long countPendingRequests(@Param("hospitalId") Long hospitalId);

    @Query("SELECT r FROM RecordShareRequest r WHERE r.patient.id = :patientId "
            + "AND r.requestingHospital.id = :requestingHospitalId " + "AND r.sourceHospital.id = :sourceHospitalId "
            + "ORDER BY r.createdAt DESC")
    List<RecordShareRequest> findRecentRequests(@Param("patientId") Long patientId,
            @Param("requestingHospitalId") Long requestingHospitalId, @Param("sourceHospitalId") Long sourceHospitalId);

    @Query("SELECT MAX(r.requestNumber) FROM RecordShareRequest r WHERE r.requestNumber LIKE :prefix%")
    String findMaxRequestNumber(@Param("prefix") String prefix);
}
