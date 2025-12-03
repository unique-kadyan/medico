package com.kaddy.repository;

import com.kaddy.model.MedicineOrder;
import com.kaddy.model.enums.MedicineOrderStatus;
import com.kaddy.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, Long> {

        Optional<MedicineOrder> findByOrderNumber(String orderNumber);

        List<MedicineOrder> findByPatientId(Long patientId);

        @Query("SELECT o FROM MedicineOrder o WHERE o.patient.user.id = :userId AND o.active = true ORDER BY o.orderDate DESC")
        List<MedicineOrder> findByPatientUserId(@Param("userId") Long userId);

        List<MedicineOrder> findByPrescriptionId(Long prescriptionId);

        List<MedicineOrder> findByStatus(MedicineOrderStatus status);

        List<MedicineOrder> findByPaymentStatus(PaymentStatus paymentStatus);

        @Query("SELECT o FROM MedicineOrder o WHERE o.status = :status AND o.active = true ORDER BY o.orderDate DESC")
        List<MedicineOrder> findActiveOrdersByStatus(@Param("status") MedicineOrderStatus status);

        @Query("SELECT o FROM MedicineOrder o WHERE o.active = true ORDER BY o.orderDate DESC")
        List<MedicineOrder> findAllActiveOrders();

        @Query("SELECT o FROM MedicineOrder o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.active = true")
        List<MedicineOrder> findOrdersBetweenDates(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT o FROM MedicineOrder o WHERE o.patient.id = :patientId AND o.status IN :statuses AND o.active = true")
        List<MedicineOrder> findByPatientIdAndStatusIn(
                        @Param("patientId") Long patientId,
                        @Param("statuses") List<MedicineOrderStatus> statuses);

        boolean existsByOrderNumber(String orderNumber);

        @Query("SELECT COUNT(o) FROM MedicineOrder o WHERE o.status = :status AND o.active = true")
        long countByStatus(@Param("status") MedicineOrderStatus status);
}
