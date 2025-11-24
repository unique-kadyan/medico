package com.kaddy.repository;

import com.kaddy.model.InventoryItem;
import com.kaddy.model.enums.InventoryCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Page<InventoryItem> findByHospitalIdAndIsActiveTrue(Long hospitalId, Pageable pageable);

    List<InventoryItem> findByHospitalIdAndCategoryAndIsActiveTrue(Long hospitalId, InventoryCategory category);

    Optional<InventoryItem> findByHospitalIdAndSku(Long hospitalId, String sku);

    Optional<InventoryItem> findByHospitalIdAndBarcode(Long hospitalId, String barcode);

    @Query("SELECT i FROM InventoryItem i WHERE i.hospital.id = :hospitalId AND i.isActive = true "
            + "AND i.currentStock <= i.reorderLevel")
    List<InventoryItem> findLowStockItems(@Param("hospitalId") Long hospitalId);

    @Query("SELECT i FROM InventoryItem i WHERE i.hospital.id = :hospitalId AND i.isActive = true "
            + "AND i.currentStock <= 0")
    List<InventoryItem> findOutOfStockItems(@Param("hospitalId") Long hospitalId);

    @Query("SELECT i FROM InventoryItem i WHERE i.hospital.id = :hospitalId AND i.isActive = true "
            + "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(i.genericName) LIKE LOWER(CONCAT('%', :search, '%')) " + "OR i.barcode LIKE %:search%)")
    Page<InventoryItem> searchItems(@Param("hospitalId") Long hospitalId, @Param("search") String search,
            Pageable pageable);

    @Query("SELECT SUM(i.currentStock * i.purchasePrice) FROM InventoryItem i WHERE i.hospital.id = :hospitalId AND i.isActive = true")
    java.math.BigDecimal getTotalStockValue(@Param("hospitalId") Long hospitalId);

    @Query("SELECT i.category, COUNT(i), SUM(i.currentStock) FROM InventoryItem i "
            + "WHERE i.hospital.id = :hospitalId AND i.isActive = true GROUP BY i.category")
    List<Object[]> getStockSummaryByCategory(@Param("hospitalId") Long hospitalId);

    boolean existsByHospitalIdAndSku(Long hospitalId, String sku);

    boolean existsByHospitalIdAndBarcode(Long hospitalId, String barcode);
}
