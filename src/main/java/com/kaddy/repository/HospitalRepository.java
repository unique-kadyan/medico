package com.kaddy.repository;

import com.kaddy.model.Hospital;
import com.kaddy.model.enums.SubscriptionPlan;
import com.kaddy.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    Optional<Hospital> findByCode(String code);

    Optional<Hospital> findByEmail(String email);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);

    List<Hospital> findBySubscriptionStatus(SubscriptionStatus status);

    List<Hospital> findBySubscriptionPlan(SubscriptionPlan plan);

    @Query("SELECT h FROM Hospital h WHERE h.subscriptionPlan = 'TRIAL' AND h.trialEndDate < :now")
    List<Hospital> findExpiredTrials(@Param("now") LocalDateTime now);

    @Query("SELECT h FROM Hospital h WHERE h.subscriptionPlan = 'TRIAL' AND h.trialEndDate BETWEEN :now AND :reminderDate")
    List<Hospital> findTrialsEndingSoon(@Param("now") LocalDateTime now,
            @Param("reminderDate") LocalDateTime reminderDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.hospital.id = :hospitalId AND u.active = true")
    long countActiveUsersByHospitalId(@Param("hospitalId") Long hospitalId);

    List<Hospital> findByActiveTrue();
}
