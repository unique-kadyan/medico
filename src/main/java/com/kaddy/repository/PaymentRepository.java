package com.kaddy.repository;

import com.kaddy.model.Payment;
import com.kaddy.model.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReceiptNumber(String receiptNumber);

    List<Payment> findByInvoiceId(Long invoiceId);

    Page<Payment> findByHospitalId(Long hospitalId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.hospital.id = :hospitalId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByHospitalIdAndDateRange(@Param("hospitalId") Long hospitalId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.hospital.id = :hospitalId AND p.paymentMethod = :method "
            + "AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByHospitalIdAndMethodAndDateRange(@Param("hospitalId") Long hospitalId,
            @Param("method") PaymentMethod method, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.hospital.id = :hospitalId "
            + "AND p.paymentDate BETWEEN :startDate AND :endDate AND p.isRefunded = false")
    BigDecimal getTotalCollections(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p WHERE p.hospital.id = :hospitalId "
            + "AND p.paymentDate BETWEEN :startDate AND :endDate AND p.isRefunded = false "
            + "GROUP BY p.paymentMethod")
    List<Object[]> getCollectionsByPaymentMethod(@Param("hospitalId") Long hospitalId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Payment> findByHospitalIdAndIsRefundedTrue(Long hospitalId);

    @Query("SELECT SUM(p.refundedAmount) FROM Payment p WHERE p.hospital.id = :hospitalId "
            + "AND p.refundedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRefunds(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MAX(p.receiptNumber) FROM Payment p WHERE p.hospital.id = :hospitalId AND p.receiptNumber LIKE :prefix%")
    String findLastReceiptNumber(@Param("hospitalId") Long hospitalId, @Param("prefix") String prefix);
}
