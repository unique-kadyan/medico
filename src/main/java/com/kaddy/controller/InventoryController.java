package com.kaddy.controller;

import com.kaddy.model.*;
import com.kaddy.model.enums.InventoryCategory;
import com.kaddy.model.enums.PurchaseOrderStatus;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final SecurityUtils securityUtils;

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<InventoryItem> createInventoryItem(@RequestParam String sku, @RequestParam String name,
            @RequestParam(required = false) String genericName, @RequestParam InventoryCategory category,
            @RequestParam String unit, @RequestParam BigDecimal purchasePrice, @RequestParam BigDecimal sellingPrice,
            @RequestParam(required = false) BigDecimal mrp, @RequestParam(required = false) BigDecimal taxRate,
            @RequestParam(required = false) String hsnCode, @RequestParam(required = false) Integer reorderLevel,
            @RequestParam(required = false) Integer reorderQuantity,
            @RequestParam(required = false) Long preferredVendorId, @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String strength, @RequestParam(required = false) String dosageForm,
            @RequestParam(required = false) Boolean requiresPrescription,
            @RequestParam(required = false) String description) {

        Long hospitalId = getCurrentUserHospitalId();
        InventoryItem item = inventoryService.createInventoryItem(hospitalId, sku, name, genericName, category, unit,
                purchasePrice, sellingPrice, mrp, taxRate, hsnCode, reorderLevel, reorderQuantity, preferredVendorId,
                manufacturer, strength, dosageForm, requiresPrescription, description);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<InventoryItem> updateInventoryItem(@PathVariable Long id,
            @RequestParam(required = false) String name, @RequestParam(required = false) BigDecimal sellingPrice,
            @RequestParam(required = false) BigDecimal mrp, @RequestParam(required = false) Integer reorderLevel,
            @RequestParam(required = false) Integer reorderQuantity, @RequestParam(required = false) String rackNumber,
            @RequestParam(required = false) String shelfNumber, @RequestParam(required = false) Boolean isActive) {

        InventoryItem item = inventoryService.updateInventoryItem(id, name, sellingPrice, mrp, reorderLevel,
                reorderQuantity, rackNumber, shelfNumber, isActive);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/items/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<InventoryItem> getInventoryItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryItem(id));
    }

    @GetMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<Page<InventoryItem>> getInventoryItems(@RequestParam(required = false) String search,
            @RequestParam(required = false) InventoryCategory category, @PageableDefault(size = 20) Pageable pageable) {

        Long hospitalId = getCurrentUserHospitalId();

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(inventoryService.searchInventoryItems(hospitalId, search, pageable));
        } else if (category != null) {
            List<InventoryItem> items = inventoryService.getInventoryItemsByCategory(hospitalId, category);
            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(items, pageable, items.size()));
        } else {
            return ResponseEntity.ok(inventoryService.getInventoryItems(hospitalId, pageable));
        }
    }

    @GetMapping("/items/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(inventoryService.getLowStockItems(hospitalId));
    }

    @GetMapping("/items/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<InventoryItem>> getOutOfStockItems() {
        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(inventoryService.getOutOfStockItems(hospitalId));
    }

    @PostMapping("/items/{itemId}/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<StockBatch> addStockBatch(@PathVariable Long itemId, @RequestParam String batchNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate manufacturingDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam Integer quantity, @RequestParam BigDecimal purchasePrice,
            @RequestParam(required = false) BigDecimal sellingPrice, @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long purchaseOrderId, @RequestParam(required = false) String notes) {

        Long userId = getCurrentUserId();
        StockBatch batch = inventoryService.addStockBatch(itemId, batchNumber, manufacturingDate, expiryDate, quantity,
                purchasePrice, sellingPrice, vendorId, purchaseOrderId, userId, notes);
        return new ResponseEntity<>(batch, HttpStatus.CREATED);
    }

    @GetMapping("/items/{itemId}/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<StockBatch>> getAvailableBatches(@PathVariable Long itemId) {
        return ResponseEntity.ok(inventoryService.getAvailableBatchesFEFO(itemId));
    }

    @GetMapping("/batches/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<StockBatch>> getExpiringSoonBatches(
            @RequestParam(defaultValue = "30") int daysThreshold) {
        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(inventoryService.getExpiringSoonBatches(hospitalId, daysThreshold));
    }

    @GetMapping("/batches/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<StockBatch>> getExpiredBatches() {
        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(inventoryService.getExpiredBatches(hospitalId));
    }

    @PostMapping("/batches/{batchId}/write-off")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<Void> writeOffExpiredBatch(@PathVariable Long batchId, @RequestParam String reason) {
        Long userId = getCurrentUserId();
        inventoryService.writeOffExpiredStock(batchId, userId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{itemId}/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<Void> adjustStock(@PathVariable Long itemId, @RequestParam Integer quantity,
            @RequestParam boolean isAddition, @RequestParam String reason) {
        Long userId = getCurrentUserId();
        inventoryService.adjustStock(itemId, quantity, userId, reason, isAddition);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{itemId}/deduct")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR')")
    public ResponseEntity<Void> deductStock(@PathVariable Long itemId, @RequestParam Integer quantity,
            @RequestParam String reason, @RequestParam(required = false) String referenceNumber) {
        Long userId = getCurrentUserId();
        inventoryService.deductStock(itemId, quantity, userId, reason, com.kaddy.model.enums.StockMovementType.SALE,
                null, referenceNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/items/{itemId}/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<StockMovement>> getStockMovements(@PathVariable Long itemId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(inventoryService.getStockMovements(itemId, PageRequest.of(0, limit)));
    }

    @PostMapping("/vendors")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Vendor> createVendor(@RequestParam String vendorCode, @RequestParam String name,
            @RequestParam(required = false) String contactPerson, @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone, @RequestParam(required = false) String address,
            @RequestParam(required = false) String gstNumber, @RequestParam(required = false) Integer paymentTermDays,
            @RequestParam(required = false) BigDecimal creditLimit) {

        Long hospitalId = getCurrentUserHospitalId();
        Vendor vendor = inventoryService.createVendor(hospitalId, vendorCode, name, contactPerson, email, phone,
                address, gstNumber, paymentTermDays, creditLimit);
        return new ResponseEntity<>(vendor, HttpStatus.CREATED);
    }

    @GetMapping("/vendors")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<?> getVendors(@RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Long hospitalId = getCurrentUserHospitalId();

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(inventoryService.searchVendors(hospitalId, search, pageable));
        } else {
            return ResponseEntity.ok(inventoryService.getActiveVendors(hospitalId));
        }
    }

    @PostMapping("/purchase-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestParam Long vendorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedDeliveryDate,
            @RequestParam(required = false) String notes) {

        Long hospitalId = getCurrentUserHospitalId();
        Long userId = getCurrentUserId();
        PurchaseOrder po = inventoryService.createPurchaseOrder(hospitalId, vendorId, userId, expectedDeliveryDate,
                notes);
        return new ResponseEntity<>(po, HttpStatus.CREATED);
    }

    @PostMapping("/purchase-orders/{poId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<PurchaseOrder> addItemToPurchaseOrder(@PathVariable Long poId, @RequestParam Long itemId,
            @RequestParam Integer quantity, @RequestParam(required = false) BigDecimal unitPrice,
            @RequestParam(required = false) BigDecimal discountPercentage,
            @RequestParam(required = false) BigDecimal taxRate, @RequestParam(required = false) String notes) {

        PurchaseOrder po = inventoryService.addItemToPurchaseOrder(poId, itemId, quantity, unitPrice,
                discountPercentage, taxRate, notes);
        return ResponseEntity.ok(po);
    }

    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<PurchaseOrder> getPurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getPurchaseOrder(id));
    }

    @GetMapping("/purchase-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<Page<PurchaseOrder>> getPurchaseOrders(
            @RequestParam(required = false) PurchaseOrderStatus status, @PageableDefault(size = 20) Pageable pageable) {

        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(inventoryService.getPurchaseOrders(hospitalId, status, pageable));
    }

    @PostMapping("/purchase-orders/{poId}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<PurchaseOrder> submitPurchaseOrder(@PathVariable Long poId) {
        return ResponseEntity.ok(inventoryService.submitPurchaseOrderForApproval(poId));
    }

    @PostMapping("/purchase-orders/{poId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<PurchaseOrder> approvePurchaseOrder(@PathVariable Long poId,
            @RequestParam(required = false) String approvalNotes) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(inventoryService.approvePurchaseOrder(poId, userId, approvalNotes));
    }

    @PostMapping("/purchase-orders/{poId}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<PurchaseOrder> sendPurchaseOrder(@PathVariable Long poId) {
        return ResponseEntity.ok(inventoryService.sendPurchaseOrder(poId));
    }

    @PostMapping("/purchase-orders/{poId}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<PurchaseOrder> receiveGoods(@PathVariable Long poId, @RequestParam Long itemId,
            @RequestParam Integer receivedQuantity, @RequestParam String batchNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate manufacturingDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam(required = false) BigDecimal purchasePrice, @RequestParam(required = false) String notes) {

        Long userId = getCurrentUserId();
        PurchaseOrder po = inventoryService.receiveGoods(poId, itemId, receivedQuantity, batchNumber, manufacturingDate,
                expiryDate, purchasePrice, userId, notes);
        return ResponseEntity.ok(po);
    }

    @PostMapping("/purchase-orders/{poId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<PurchaseOrder> cancelPurchaseOrder(@PathVariable Long poId, @RequestParam String reason) {
        return ResponseEntity.ok(inventoryService.cancelPurchaseOrder(poId, reason));
    }

    @GetMapping("/purchase-orders/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PurchaseOrder>> getOverdueOrders() {
        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(inventoryService.getOverdueOrders(hospitalId));
    }

    @GetMapping("/reports/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getInventoryReport() {
        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(inventoryService.getInventoryReport(hospitalId));
    }

    private Long getCurrentUserHospitalId() {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        if (currentUser.getHospital() == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }
        return currentUser.getHospital().getId();
    }

    private Long getCurrentUserId() {
        return securityUtils.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
    }
}
