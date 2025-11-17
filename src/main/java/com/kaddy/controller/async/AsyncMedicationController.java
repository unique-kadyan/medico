package com.kaddy.controller.async;

import com.kaddy.dto.MedicationDTO;
import com.kaddy.service.async.AsyncMedicationService;
import com.kaddy.service.async.AsyncMedicationService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Async Medication Controller with parallel processing
 */
@RestController
@RequestMapping("/api/async/medications")
@RequiredArgsConstructor
@Tag(name = "Async Medication Management", description = "High-performance async medication APIs with multi-threading")
public class AsyncMedicationController {

    private final AsyncMedicationService asyncMedicationService;

    @GetMapping
    @Operation(summary = "Get all medications asynchronously",
               description = "Retrieve all medications with parallel stream processing")
    public CompletableFuture<ResponseEntity<List<MedicationDTO>>> getAllMedicationsAsync() {
        return asyncMedicationService.getAllMedicationsAsync()
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get inventory alerts",
               description = "Calculate low stock, expired, and expiring soon medications in parallel")
    public CompletableFuture<ResponseEntity<InventoryAlerts>> getInventoryAlertsAsync() {
        return asyncMedicationService.getInventoryAlertsAsync()
            .thenApply(ResponseEntity::ok);
    }

    @PatchMapping("/stock/batch")
    @Operation(summary = "Batch update medication stock",
               description = "Update stock levels for multiple medications concurrently")
    public CompletableFuture<ResponseEntity<List<MedicationDTO>>> batchUpdateStockAsync(
            @RequestBody Map<Long, Integer> stockUpdates) {
        return asyncMedicationService.batchUpdateStockAsync(stockUpdates)
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/report")
    @Operation(summary = "Generate comprehensive medication report",
               description = "Generate detailed medication analytics using parallel computation")
    public CompletableFuture<ResponseEntity<MedicationReport>> generateReportAsync() {
        return asyncMedicationService.generateReportAsync()
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/search/advanced")
    @Operation(summary = "Advanced medication search",
               description = "Search medications with multiple criteria using functional composition")
    public CompletableFuture<ResponseEntity<List<MedicationDTO>>> advancedSearchAsync(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) Boolean expired) {

        return asyncMedicationService.advancedSearchAsync(name, category, lowStock, expired)
            .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/bulk-import")
    @Operation(summary = "Bulk import medications",
               description = "Import large number of medications with batch processing")
    public CompletableFuture<ResponseEntity<BulkImportResult>> bulkImportAsync(
            @Valid @RequestBody List<MedicationDTO> medicationDTOs) {
        return asyncMedicationService.bulkImportMedicationsAsync(medicationDTOs)
            .thenApply(result -> ResponseEntity.status(201).body(result));
    }

    @GetMapping("/reorder/suggestions")
    @Operation(summary = "Generate reorder suggestions",
               description = "Automatically generate reorder suggestions for low stock items")
    public CompletableFuture<ResponseEntity<List<ReorderSuggestion>>> getReorderSuggestionsAsync() {
        return asyncMedicationService.generateReorderSuggestionsAsync()
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/combined-report")
    @Operation(summary = "Get combined inventory report",
               description = "Combine multiple async operations into comprehensive report")
    public CompletableFuture<ResponseEntity<CombinedInventoryReport>> getCombinedReportAsync() {
        // Combine multiple async operations
        CompletableFuture<MedicationReport> reportFuture = asyncMedicationService.generateReportAsync();
        CompletableFuture<InventoryAlerts> alertsFuture = asyncMedicationService.getInventoryAlertsAsync();
        CompletableFuture<List<ReorderSuggestion>> reorderFuture = asyncMedicationService.generateReorderSuggestionsAsync();

        return CompletableFuture.allOf(reportFuture, alertsFuture, reorderFuture)
            .thenApply(v -> {
                CombinedInventoryReport combined = new CombinedInventoryReport(
                    reportFuture.join(),
                    alertsFuture.join(),
                    reorderFuture.join()
                );
                return ResponseEntity.ok(combined);
            });
    }

    // Combined report DTO
    public record CombinedInventoryReport(
        MedicationReport report,
        InventoryAlerts alerts,
        List<ReorderSuggestion> reorderSuggestions
    ) {}
}
