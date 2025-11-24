package com.kaddy.repository;

import com.kaddy.model.StockBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {

    List<StockBatch> findByInventoryItemIdAndIsActiveTrueAndCurrentQuantityGreaterThan(Long itemId,
            Integer minQuantity);

    @Query("SELECT sb FROM StockBatch sb WHERE sb.inventoryItem.id = :itemId AND sb.isActive = true "
            + "AND sb.currentQuantity > 0 ORDER BY sb.manufacturingDate ASC")
    List<StockBatch> findAvailableBatchesFIFO(@Param("itemId") Long itemId);

    @Query("SELECT sb FROM StockBatch sb WHERE sb.inventoryItem.id = :itemId AND sb.isActive = true "
            + "AND sb.currentQuantity > 0 ORDER BY sb.expiryDate ASC")
    List<StockBatch> findAvailableBatchesFEFO(@Param("itemId") Long itemId);

    @Query("SELECT sb FROM StockBatch sb WHERE sb.hospital.id = :hospitalId AND sb.isActive = true "
            + "AND sb.currentQuantity > 0 AND sb.expiryDate <= :expiryDate")
    List<StockBatch> findExpiringSoon(@Param("hospitalId") Long hospitalId, @Param("expiryDate") LocalDate expiryDate);

    @Query("SELECT sb FROM StockBatch sb WHERE sb.hospital.id = :hospitalId AND sb.isActive = true "
            + "AND sb.currentQuantity > 0 AND sb.expiryDate < :today")
    List<StockBatch> findExpiredBatches(@Param("hospitalId") Long hospitalId, @Param("today") LocalDate today);

    Optional<StockBatch> findByInventoryItemIdAndBatchNumber(Long itemId, String batchNumber);

    Page<StockBatch> findByHospitalId(Long hospitalId, Pageable pageable);

    @Query("SELECT SUM(sb.currentQuantity) FROM StockBatch sb WHERE sb.inventoryItem.id = :itemId AND sb.isActive = true")
    Integer getTotalQuantityByItem(@Param("itemId") Long itemId);
}
