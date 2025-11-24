package com.kaddy.repository;

import com.kaddy.model.OTRequest;
import com.kaddy.model.enums.OTRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OTRequestRepository extends JpaRepository<OTRequest, Long> {

    List<OTRequest> findBySurgeonId(Long surgeonId);

    List<OTRequest> findByPatientId(Long patientId);

    List<OTRequest> findByStatus(OTRequestStatus status);

    @Query("SELECT o FROM OTRequest o WHERE o.scheduledStartTime BETWEEN :startDate AND :endDate ORDER BY o.scheduledStartTime")
    List<OTRequest> findByScheduledStartTimeBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM OTRequest o WHERE o.surgeon.id = :surgeonId AND o.status = :status")
    List<OTRequest> findBySurgeonIdAndStatus(@Param("surgeonId") Long surgeonId,
            @Param("status") OTRequestStatus status);

    @Query("SELECT o FROM OTRequest o WHERE o.status IN (:statuses) ORDER BY o.scheduledStartTime")
    List<OTRequest> findByStatusIn(@Param("statuses") List<OTRequestStatus> statuses);

    @Query("SELECT o FROM OTRequest o WHERE o.isEmergency = true AND o.status = 'PENDING' ORDER BY o.createdAt DESC")
    List<OTRequest> findEmergencyPendingRequests();

    @Query("SELECT o FROM OTRequest o WHERE o.otRoomNumber = :roomNumber "
            + "AND o.status IN ('APPROVED', 'IN_PROGRESS') "
            + "AND o.scheduledStartTime BETWEEN :startTime AND :endTime")
    List<OTRequest> findConflictingRequests(@Param("roomNumber") String roomNumber,
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
