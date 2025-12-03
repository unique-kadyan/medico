package com.kaddy.repository;

import com.kaddy.model.MedicineOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineOrderPaymentRepository extends JpaRepository<MedicineOrderPayment, Long> {

        Optional<MedicineOrderPayment> findByReceiptNumber(String receiptNumber);

        List<MedicineOrderPayment> findByOrderId(Long orderId);

        @Query("SELECT p FROM MedicineOrderPayment p WHERE p.order.id = :orderId AND p.active = true ORDER BY p.paymentDate DESC")
        List<MedicineOrderPayment> findActivePaymentsByOrderId(@Param("orderId") Long orderId);

        @Query("SELECT p FROM MedicineOrderPayment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.active = true")
        List<MedicineOrderPayment> findPaymentsBetweenDates(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT p FROM MedicineOrderPayment p WHERE p.receivedBy.id = :userId AND p.active = true ORDER BY p.paymentDate DESC")
        List<MedicineOrderPayment> findByReceivedByUserId(@Param("userId") Long userId);

        @Query("SELECT SUM(p.amount) FROM MedicineOrderPayment p WHERE p.order.id = :orderId AND p.isRefunded = false AND p.active = true")
        BigDecimal getTotalPaidForOrder(@Param("orderId") Long orderId);

        @Query("SELECT SUM(p.amount) FROM MedicineOrderPayment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.isRefunded = false AND p.active = true")
        BigDecimal getTotalCollectionBetweenDates(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT p.paymentMethod, SUM(p.amount) FROM MedicineOrderPayment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.isRefunded = false AND p.active = true GROUP BY p.paymentMethod")
        List<Object[]> getPaymentMethodBreakdown(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        boolean existsByReceiptNumber(String receiptNumber);

        @Query("SELECT p.receiptNumber FROM MedicineOrderPayment p WHERE p.receiptNumber LIKE :prefix% ORDER BY p.receiptNumber DESC LIMIT 1")
        String findLastReceiptNumber(@Param("prefix") String prefix);
}
