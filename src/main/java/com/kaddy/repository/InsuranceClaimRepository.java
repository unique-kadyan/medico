package com.kaddy.repository;

import com.kaddy.model.InsuranceClaim;
import com.kaddy.model.enums.InsuranceClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {

    Optional<InsuranceClaim> findByClaimNumber(String claimNumber);

    Page<InsuranceClaim> findByHospitalId(Long hospitalId, Pageable pageable);

    Page<InsuranceClaim> findByHospitalIdAndStatus(Long hospitalId, InsuranceClaimStatus status, Pageable pageable);

    List<InsuranceClaim> findByPatientId(Long patientId);

    List<InsuranceClaim> findByInvoiceId(Long invoiceId);

    @Query("SELECT ic FROM InsuranceClaim ic WHERE ic.hospital.id = :hospitalId "
            + "AND ic.claimDate BETWEEN :startDate AND :endDate")
    List<InsuranceClaim> findByHospitalIdAndDateRange(@Param("hospitalId") Long hospitalId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ic FROM InsuranceClaim ic WHERE ic.hospital.id = :hospitalId "
            + "AND ic.status IN ('SUBMITTED', 'UNDER_REVIEW')")
    List<InsuranceClaim> findPendingClaims(@Param("hospitalId") Long hospitalId);

    @Query("SELECT SUM(ic.claimedAmount) FROM InsuranceClaim ic WHERE ic.hospital.id = :hospitalId "
            + "AND ic.claimDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalClaimedAmount(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(ic.settledAmount) FROM InsuranceClaim ic WHERE ic.hospital.id = :hospitalId "
            + "AND ic.settlementDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSettledAmount(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(ic.rejectedAmount) FROM InsuranceClaim ic WHERE ic.hospital.id = :hospitalId "
            + "AND ic.status = 'REJECTED' AND ic.responseDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRejectedAmount(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(ic.claimNumber) FROM InsuranceClaim ic WHERE ic.hospital.id = :hospitalId AND ic.claimNumber LIKE :prefix%")
    String findLastClaimNumber(@Param("hospitalId") Long hospitalId, @Param("prefix") String prefix);
}
