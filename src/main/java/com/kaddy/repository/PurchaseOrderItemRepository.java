package com.kaddy.repository;

import com.kaddy.model.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);

    List<PurchaseOrderItem> findByInventoryItemId(Long inventoryItemId);

    @Query("SELECT poi FROM PurchaseOrderItem poi WHERE poi.purchaseOrder.id = :poId "
            + "AND poi.receivedQuantity < poi.orderedQuantity")
    List<PurchaseOrderItem> findPendingItems(@Param("poId") Long purchaseOrderId);

    @Query("SELECT poi FROM PurchaseOrderItem poi WHERE poi.purchaseOrder.id = :poId "
            + "AND poi.receivedQuantity >= poi.orderedQuantity")
    List<PurchaseOrderItem> findReceivedItems(@Param("poId") Long purchaseOrderId);
}
