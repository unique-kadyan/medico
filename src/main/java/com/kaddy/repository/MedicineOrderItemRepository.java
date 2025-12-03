package com.kaddy.repository;

import com.kaddy.model.MedicineOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineOrderItemRepository extends JpaRepository<MedicineOrderItem, Long> {

    List<MedicineOrderItem> findByOrderId(Long orderId);

    List<MedicineOrderItem> findByMedicationId(Long medicationId);

    @Query("SELECT oi FROM MedicineOrderItem oi WHERE oi.order.id = :orderId AND oi.active = true")
    List<MedicineOrderItem> findActiveItemsByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT SUM(oi.quantity) FROM MedicineOrderItem oi WHERE oi.medication.id = :medicationId AND oi.order.active = true")
    Long getTotalQuantityOrderedForMedication(@Param("medicationId") Long medicationId);
}
