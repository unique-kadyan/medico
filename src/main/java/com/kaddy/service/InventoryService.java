package com.kaddy.service;

import com.kaddy.model.*;
import com.kaddy.model.enums.InventoryCategory;
import com.kaddy.model.enums.PurchaseOrderStatus;
import com.kaddy.model.enums.StockMovementType;
import com.kaddy.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockBatchRepository stockBatchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    public InventoryItem createInventoryItem(Long hospitalId, String sku, String name, String genericName,
            InventoryCategory category, String unit, BigDecimal purchasePrice, BigDecimal sellingPrice, BigDecimal mrp,
            BigDecimal taxRate, String hsnCode, Integer reorderLevel, Integer reorderQuantity, Long preferredVendorId,
            String manufacturer, String strength, String dosageForm, Boolean requiresPrescription, String description) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));

        if (inventoryItemRepository.existsByHospitalIdAndSku(hospitalId, sku)) {
            throw new IllegalArgumentException("SKU already exists: " + sku);
        }

        InventoryItem item = new InventoryItem();
        item.setHospital(hospital);
        item.setSku(sku);
        item.setName(name);
        item.setGenericName(genericName);
        item.setCategory(category);
        item.setUnit(unit);
        item.setPurchasePrice(purchasePrice);
        item.setSellingPrice(sellingPrice);
        item.setMrp(mrp);
        item.setTaxRate(taxRate != null ? taxRate : BigDecimal.ZERO);
        item.setHsnCode(hsnCode);
        item.setReorderLevel(reorderLevel != null ? reorderLevel : 10);
        item.setReorderQuantity(reorderQuantity != null ? reorderQuantity : 50);
        item.setManufacturer(manufacturer);
        item.setStrength(strength);
        item.setDosageForm(dosageForm);
        item.setRequiresPrescription(requiresPrescription != null ? requiresPrescription : false);
        item.setDescription(description);
        item.setCurrentStock(0);
        item.setIsActive(true);

        if (preferredVendorId != null) {
            Vendor vendor = vendorRepository.findById(preferredVendorId)
                    .orElseThrow(() -> new EntityNotFoundException("Vendor not found"));
            item.setPreferredVendor(vendor);
        }

        log.info("Created inventory item: {} (SKU: {})", name, sku);
        return inventoryItemRepository.save(item);
    }

    public InventoryItem updateInventoryItem(Long itemId, String name, BigDecimal sellingPrice, BigDecimal mrp,
            Integer reorderLevel, Integer reorderQuantity, String rackNumber, String shelfNumber, Boolean isActive) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found"));

        if (name != null)
            item.setName(name);
        if (sellingPrice != null)
            item.setSellingPrice(sellingPrice);
        if (mrp != null)
            item.setMrp(mrp);
        if (reorderLevel != null)
            item.setReorderLevel(reorderLevel);
        if (reorderQuantity != null)
            item.setReorderQuantity(reorderQuantity);
        if (rackNumber != null)
            item.setRackNumber(rackNumber);
        if (shelfNumber != null)
            item.setShelfNumber(shelfNumber);
        if (isActive != null)
            item.setIsActive(isActive);

        return inventoryItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public InventoryItem getInventoryItem(Long itemId) {
        return inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found"));
    }

    @Transactional(readOnly = true)
    public Page<InventoryItem> getInventoryItems(Long hospitalId, Pageable pageable) {
        return inventoryItemRepository.findByHospitalIdAndIsActiveTrue(hospitalId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<InventoryItem> searchInventoryItems(Long hospitalId, String search, Pageable pageable) {
        return inventoryItemRepository.searchItems(hospitalId, search, pageable);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getInventoryItemsByCategory(Long hospitalId, InventoryCategory category) {
        return inventoryItemRepository.findByHospitalIdAndCategoryAndIsActiveTrue(hospitalId, category);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getLowStockItems(Long hospitalId) {
        return inventoryItemRepository.findLowStockItems(hospitalId);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getOutOfStockItems(Long hospitalId) {
        return inventoryItemRepository.findOutOfStockItems(hospitalId);
    }

    public StockBatch addStockBatch(Long itemId, String batchNumber, LocalDate manufacturingDate, LocalDate expiryDate,
            Integer quantity, BigDecimal purchasePrice, BigDecimal sellingPrice, Long vendorId, Long purchaseOrderId,
            Long userId, String notes) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found"));

        StockBatch batch = new StockBatch();
        batch.setHospital(item.getHospital());
        batch.setInventoryItem(item);
        batch.setBatchNumber(batchNumber);
        batch.setManufacturingDate(manufacturingDate);
        batch.setExpiryDate(expiryDate);
        batch.setInitialQuantity(quantity);
        batch.setCurrentQuantity(quantity);
        batch.setPurchasePrice(purchasePrice);
        batch.setSellingPrice(sellingPrice != null ? sellingPrice : item.getSellingPrice());
        batch.setReceivedDate(LocalDate.now());
        batch.setNotes(notes);
        batch.setIsActive(true);

        if (vendorId != null) {
            batch.setVendor(vendorRepository.findById(vendorId).orElse(null));
        }
        if (purchaseOrderId != null) {
            batch.setPurchaseOrder(purchaseOrderRepository.findById(purchaseOrderId).orElse(null));
        }

        batch = stockBatchRepository.save(batch);

        int previousStock = item.getCurrentStock();
        item.setCurrentStock(previousStock + quantity);
        inventoryItemRepository.save(item);

        recordStockMovement(item, batch, StockMovementType.PURCHASE, quantity, purchasePrice, previousStock,
                item.getCurrentStock(), purchaseOrderId, null, userId, notes);

        log.info("Added stock batch {} for item {}: {} units", batchNumber, item.getName(), quantity);
        return batch;
    }

    @Transactional(readOnly = true)
    public List<StockBatch> getAvailableBatchesFEFO(Long itemId) {
        return stockBatchRepository.findAvailableBatchesFEFO(itemId);
    }

    @Transactional(readOnly = true)
    public List<StockBatch> getExpiringSoonBatches(Long hospitalId, int daysThreshold) {
        LocalDate expiryDate = LocalDate.now().plusDays(daysThreshold);
        return stockBatchRepository.findExpiringSoon(hospitalId, expiryDate);
    }

    @Transactional(readOnly = true)
    public List<StockBatch> getExpiredBatches(Long hospitalId) {
        return stockBatchRepository.findExpiredBatches(hospitalId, LocalDate.now());
    }

    public void deductStock(Long itemId, int quantity, Long userId, String reason, StockMovementType movementType,
            Long referenceId, String referenceNumber) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found"));

        if (item.getCurrentStock() < quantity) {
            throw new IllegalStateException("Insufficient stock. Available: " + item.getCurrentStock());
        }

        int remainingQuantity = quantity;
        List<StockBatch> batches = stockBatchRepository.findAvailableBatchesFEFO(itemId);

        for (StockBatch batch : batches) {
            if (remainingQuantity <= 0)
                break;

            int deductFromBatch = Math.min(batch.getCurrentQuantity(), remainingQuantity);
            batch.setCurrentQuantity(batch.getCurrentQuantity() - deductFromBatch);
            stockBatchRepository.save(batch);

            remainingQuantity -= deductFromBatch;
        }

        int previousStock = item.getCurrentStock();
        item.setCurrentStock(previousStock - quantity);
        inventoryItemRepository.save(item);

        recordStockMovement(item, null, movementType, -quantity, item.getSellingPrice(), previousStock,
                item.getCurrentStock(), null, referenceNumber, userId, reason);

        log.info("Deducted {} units from item {}", quantity, item.getName());
    }

    public void adjustStock(Long itemId, int adjustmentQuantity, Long userId, String reason, boolean isAddition) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found"));

        int previousStock = item.getCurrentStock();
        int newStock = isAddition ? previousStock + adjustmentQuantity : previousStock - adjustmentQuantity;

        if (newStock < 0) {
            throw new IllegalStateException("Stock cannot be negative");
        }

        item.setCurrentStock(newStock);
        inventoryItemRepository.save(item);

        StockMovementType type = isAddition ? StockMovementType.ADJUSTMENT_ADD : StockMovementType.ADJUSTMENT_REMOVE;
        recordStockMovement(item, null, type, isAddition ? adjustmentQuantity : -adjustmentQuantity,
                item.getPurchasePrice(), previousStock, newStock, null, null, userId, reason);

        log.info("Stock adjusted for item {}: {} -> {}", item.getName(), previousStock, newStock);
    }

    public void writeOffExpiredStock(Long batchId, Long userId, String reason) {
        StockBatch batch = stockBatchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("Stock batch not found"));

        if (batch.getCurrentQuantity() <= 0) {
            throw new IllegalStateException("Batch has no stock to write off");
        }

        int quantity = batch.getCurrentQuantity();
        InventoryItem item = batch.getInventoryItem();
        int previousStock = item.getCurrentStock();

        batch.setCurrentQuantity(0);
        batch.setIsActive(false);
        stockBatchRepository.save(batch);

        item.setCurrentStock(previousStock - quantity);
        inventoryItemRepository.save(item);

        recordStockMovement(item, batch, StockMovementType.EXPIRED, -quantity, batch.getPurchasePrice(), previousStock,
                item.getCurrentStock(), null, null, userId, reason);

        log.info("Written off expired batch {} for item {}: {} units", batch.getBatchNumber(), item.getName(),
                quantity);
    }

    public Vendor createVendor(Long hospitalId, String vendorCode, String name, String contactPerson, String email,
            String phone, String address, String gstNumber, Integer paymentTermDays, BigDecimal creditLimit) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));

        if (vendorRepository.existsByHospitalIdAndVendorCode(hospitalId, vendorCode)) {
            throw new IllegalArgumentException("Vendor code already exists: " + vendorCode);
        }

        Vendor vendor = new Vendor();
        vendor.setHospital(hospital);
        vendor.setVendorCode(vendorCode);
        vendor.setName(name);
        vendor.setContactPerson(contactPerson);
        vendor.setEmail(email);
        vendor.setPhone(phone);
        vendor.setAddress(address);
        vendor.setGstNumber(gstNumber);
        vendor.setCreditDays(paymentTermDays != null ? paymentTermDays : 30);
        vendor.setCreditLimit(creditLimit);
        vendor.setIsActive(true);

        log.info("Created vendor: {} (Code: {})", name, vendorCode);
        return vendorRepository.save(vendor);
    }

    @Transactional(readOnly = true)
    public List<Vendor> getActiveVendors(Long hospitalId) {
        return vendorRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
    }

    @Transactional(readOnly = true)
    public Page<Vendor> searchVendors(Long hospitalId, String search, Pageable pageable) {
        return vendorRepository.searchVendors(hospitalId, search, pageable);
    }

    public PurchaseOrder createPurchaseOrder(Long hospitalId, Long vendorId, Long userId,
            LocalDate expectedDeliveryDate, String notes) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePoNumber(hospitalId));
        po.setHospital(hospital);
        po.setVendor(vendor);
        po.setCreatedBy(user);
        po.setOrderDate(LocalDate.now());
        po.setExpectedDeliveryDate(expectedDeliveryDate);
        po.setStatus(PurchaseOrderStatus.DRAFT);
        po.setNotes(notes);
        po.setSubtotal(BigDecimal.ZERO);
        po.setTotalAmount(BigDecimal.ZERO);
        po.setPaidAmount(BigDecimal.ZERO);
        po.setBalanceAmount(BigDecimal.ZERO);

        log.info("Created purchase order: {}", po.getPoNumber());
        return purchaseOrderRepository.save(po);
    }

    public PurchaseOrder addItemToPurchaseOrder(Long poId, Long itemId, Integer quantity, BigDecimal unitPrice,
            BigDecimal discountPercentage, BigDecimal taxRate, String notes) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify non-draft purchase order");
        }

        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found"));

        PurchaseOrderItem poItem = new PurchaseOrderItem();
        poItem.setPurchaseOrder(po);
        poItem.setInventoryItem(item);
        poItem.setItemName(item.getName());
        poItem.setOrderedQuantity(quantity);
        poItem.setReceivedQuantity(0);
        poItem.setPendingQuantity(quantity);
        poItem.setUnit(item.getUnit());
        poItem.setUnitPrice(unitPrice != null ? unitPrice : item.getPurchasePrice());
        poItem.setDiscountPercentage(discountPercentage != null ? discountPercentage : BigDecimal.ZERO);
        poItem.setTaxRate(taxRate != null ? taxRate : item.getTaxRate());
        poItem.setHsnCode(item.getHsnCode());
        poItem.setNotes(notes);

        po.addItem(poItem);
        return purchaseOrderRepository.save(po);
    }

    public PurchaseOrder submitPurchaseOrderForApproval(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Only draft orders can be submitted for approval");
        }

        if (po.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot submit empty purchase order");
        }

        po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        log.info("Purchase order {} submitted for approval", po.getPoNumber());
        return purchaseOrderRepository.save(po);
    }

    public PurchaseOrder approvePurchaseOrder(Long poId, Long approverId, String approvalNotes) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending orders can be approved");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovedBy(approver);
        po.setApprovedAt(LocalDateTime.now());
        po.setApprovalNotes(approvalNotes);

        log.info("Purchase order {} approved by {} {}", po.getPoNumber(), approver.getFirstName(),
                approver.getLastName());
        return purchaseOrderRepository.save(po);
    }

    public PurchaseOrder sendPurchaseOrder(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new IllegalStateException("Only approved orders can be sent");
        }

        po.setStatus(PurchaseOrderStatus.ORDERED);
        log.info("Purchase order {} sent to vendor", po.getPoNumber());
        return purchaseOrderRepository.save(po);
    }

    public PurchaseOrder receiveGoods(Long poId, Long itemId, Integer receivedQuantity, String batchNumber,
            LocalDate manufacturingDate, LocalDate expiryDate, BigDecimal purchasePrice, Long userId, String notes) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() != PurchaseOrderStatus.ORDERED && po.getStatus() != PurchaseOrderStatus.PARTIAL_RECEIVED) {
            throw new IllegalStateException("Cannot receive goods for this order status");
        }

        PurchaseOrderItem poItem = po.getItems().stream().filter(item -> item.getInventoryItem().getId().equals(itemId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Item not found in purchase order"));

        int totalReceived = poItem.getReceivedQuantity() + receivedQuantity;
        if (totalReceived > poItem.getOrderedQuantity()) {
            throw new IllegalArgumentException("Received quantity exceeds ordered quantity");
        }

        poItem.setReceivedQuantity(totalReceived);
        poItem.setPendingQuantity(poItem.getOrderedQuantity() - totalReceived);

        addStockBatch(itemId, batchNumber, manufacturingDate, expiryDate, receivedQuantity,
                purchasePrice != null ? purchasePrice : poItem.getUnitPrice(), null, po.getVendor().getId(), poId,
                userId, notes);

        boolean allReceived = po.getItems().stream()
                .allMatch(item -> item.getReceivedQuantity() >= item.getOrderedQuantity());

        if (allReceived) {
            po.setStatus(PurchaseOrderStatus.RECEIVED);
            po.setActualDeliveryDate(LocalDate.now());
        } else {
            po.setStatus(PurchaseOrderStatus.PARTIAL_RECEIVED);
        }

        log.info("Received {} units for item {} on PO {}", receivedQuantity, poItem.getItemName(), po.getPoNumber());
        return purchaseOrderRepository.save(po);
    }

    public PurchaseOrder cancelPurchaseOrder(Long poId, String reason) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED || po.getStatus() == PurchaseOrderStatus.CLOSED) {
            throw new IllegalStateException("Cannot cancel completed purchase order");
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        po.setNotes(po.getNotes() + "\nCancellation reason: " + reason);

        log.info("Purchase order {} cancelled: {}", po.getPoNumber(), reason);
        return purchaseOrderRepository.save(po);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getPurchaseOrder(Long poId) {
        return purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found"));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getPurchaseOrders(Long hospitalId, PurchaseOrderStatus status, Pageable pageable) {
        if (status != null) {
            return purchaseOrderRepository.findByHospitalIdAndStatus(hospitalId, status, pageable);
        }
        return purchaseOrderRepository.findByHospitalId(hospitalId, pageable);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOverdueOrders(Long hospitalId) {
        return purchaseOrderRepository.findOverdueOrders(hospitalId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryReport(Long hospitalId) {
        Map<String, Object> report = new HashMap<>();

        report.put("totalStockValue", inventoryItemRepository.getTotalStockValue(hospitalId));
        report.put("lowStockItems", inventoryItemRepository.findLowStockItems(hospitalId).size());
        report.put("outOfStockItems", inventoryItemRepository.findOutOfStockItems(hospitalId).size());
        report.put("expiringSoon",
                stockBatchRepository.findExpiringSoon(hospitalId, LocalDate.now().plusDays(30)).size());
        report.put("expired", stockBatchRepository.findExpiredBatches(hospitalId, LocalDate.now()).size());
        report.put("stockByCategory", inventoryItemRepository.getStockSummaryByCategory(hospitalId));

        return report;
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getStockMovements(Long itemId, Pageable pageable) {
        return stockMovementRepository.findRecentMovements(itemId, pageable);
    }

    private void recordStockMovement(InventoryItem item, StockBatch batch, StockMovementType type, int quantity,
            BigDecimal unitPrice, int previousStock, int newStock, Long purchaseOrderId, String referenceNumber,
            Long userId, String notes) {
        StockMovement movement = new StockMovement();
        movement.setHospital(item.getHospital());
        movement.setInventoryItem(item);
        movement.setStockBatch(batch);
        movement.setMovementType(type);
        movement.setMovementDate(LocalDateTime.now());
        movement.setQuantity(quantity);
        movement.setUnitPrice(unitPrice);
        movement.setTotalAmount(unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(Math.abs(quantity))) : null);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReferenceNumber(referenceNumber);
        movement.setNotes(notes);

        if (purchaseOrderId != null) {
            movement.setPurchaseOrder(purchaseOrderRepository.findById(purchaseOrderId).orElse(null));
            movement.setReferenceType("PURCHASE_ORDER");
            movement.setReferenceId(purchaseOrderId);
        }

        if (userId != null) {
            movement.setPerformedBy(userRepository.findById(userId).orElse(null));
        }

        stockMovementRepository.save(movement);
    }

    private String generatePoNumber(Long hospitalId) {
        String prefix = "PO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String lastNumber = purchaseOrderRepository.findLastPoNumber(hospitalId, prefix);

        int sequence = 1;
        if (lastNumber != null) {
            String seqStr = lastNumber.substring(prefix.length());
            sequence = Integer.parseInt(seqStr) + 1;
        }

        return prefix + String.format("%04d", sequence);
    }
}
