package com.kaddy.repository;

import com.kaddy.model.PurchaseOrder;
import com.kaddy.model.enums.PurchaseOrderStatus;
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
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    Page<PurchaseOrder> findByHospitalId(Long hospitalId, Pageable pageable);

    Page<PurchaseOrder> findByHospitalIdAndStatus(Long hospitalId, PurchaseOrderStatus status, Pageable pageable);

    List<PurchaseOrder> findByVendorId(Long vendorId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.hospital.id = :hospitalId "
            + "AND po.orderDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findByHospitalIdAndDateRange(@Param("hospitalId") Long hospitalId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<PurchaseOrder> findByHospitalIdAndStatus(Long hospitalId, PurchaseOrderStatus status);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.hospital.id = :hospitalId "
            + "AND po.status = 'ORDERED' AND po.expectedDeliveryDate < :today")
    List<PurchaseOrder> findOverdueOrders(@Param("hospitalId") Long hospitalId, @Param("today") LocalDate today);

    @Query("SELECT SUM(po.totalAmount) FROM PurchaseOrder po WHERE po.hospital.id = :hospitalId "
            + "AND po.orderDate BETWEEN :startDate AND :endDate AND po.status NOT IN ('DRAFT', 'CANCELLED')")
    BigDecimal getTotalPurchases(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(po.poNumber) FROM PurchaseOrder po WHERE po.hospital.id = :hospitalId AND po.poNumber LIKE :prefix%")
    String findLastPoNumber(@Param("hospitalId") Long hospitalId, @Param("prefix") String prefix);
}
