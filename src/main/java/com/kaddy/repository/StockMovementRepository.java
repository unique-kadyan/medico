package com.kaddy.repository;

import com.kaddy.model.StockMovement;
import com.kaddy.model.enums.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByHospitalId(Long hospitalId, Pageable pageable);

    List<StockMovement> findByInventoryItemId(Long itemId);

    Page<StockMovement> findByInventoryItemId(Long itemId, Pageable pageable);

    List<StockMovement> findByStockBatchId(Long batchId);

    Page<StockMovement> findByHospitalIdAndMovementType(Long hospitalId, StockMovementType movementType,
            Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.hospital.id = :hospitalId "
            + "AND sm.movementDate BETWEEN :startDate AND :endDate ORDER BY sm.movementDate DESC")
    List<StockMovement> findByHospitalIdAndDateRange(@Param("hospitalId") Long hospitalId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(sm.totalAmount) FROM StockMovement sm WHERE sm.hospital.id = :hospitalId "
            + "AND sm.movementType = :movementType AND sm.movementDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalValueByType(@Param("hospitalId") Long hospitalId,
            @Param("movementType") StockMovementType movementType, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sm.movementType, COUNT(sm), SUM(sm.quantity) FROM StockMovement sm "
            + "WHERE sm.hospital.id = :hospitalId AND sm.movementDate BETWEEN :startDate AND :endDate "
            + "GROUP BY sm.movementType")
    List<Object[]> getMovementSummary(@Param("hospitalId") Long hospitalId, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.inventoryItem.id = :itemId ORDER BY sm.movementDate DESC")
    List<StockMovement> findRecentMovements(@Param("itemId") Long itemId, Pageable pageable);
}
