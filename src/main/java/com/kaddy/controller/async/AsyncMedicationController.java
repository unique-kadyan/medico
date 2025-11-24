package com.kaddy.controller.async;

import com.kaddy.dto.MedicationDTO;
import com.kaddy.service.async.AsyncMedicationService;
import com.kaddy.service.async.AsyncMedicationService.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/async/medications")
@RequiredArgsConstructor
public class AsyncMedicationController {

    private final AsyncMedicationService asyncMedicationService;

    @GetMapping
    public CompletableFuture<ResponseEntity<List<MedicationDTO>>> getAllMedicationsAsync() {
        return asyncMedicationService.getAllMedicationsAsync().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/alerts")
    public CompletableFuture<ResponseEntity<InventoryAlerts>> getInventoryAlertsAsync() {
        return asyncMedicationService.getInventoryAlertsAsync().thenApply(ResponseEntity::ok);
    }

    @PatchMapping("/stock/batch")
    public CompletableFuture<ResponseEntity<List<MedicationDTO>>> batchUpdateStockAsync(
            @RequestBody Map<Long, Integer> stockUpdates) {
        return asyncMedicationService.batchUpdateStockAsync(stockUpdates).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/report")
    public CompletableFuture<ResponseEntity<MedicationReport>> generateReportAsync() {
        return asyncMedicationService.generateReportAsync().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/search/advanced")
    public CompletableFuture<ResponseEntity<List<MedicationDTO>>> advancedSearchAsync(
            @RequestParam(required = false) String name, @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock, @RequestParam(required = false) Boolean expired) {

        return asyncMedicationService.advancedSearchAsync(name, category, lowStock, expired)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/bulk-import")
    public CompletableFuture<ResponseEntity<BulkImportResult>> bulkImportAsync(
            @Valid @RequestBody List<MedicationDTO> medicationDTOs) {
        return asyncMedicationService.bulkImportMedicationsAsync(medicationDTOs)
                .thenApply(result -> ResponseEntity.status(201).body(result));
    }

    @GetMapping("/reorder/suggestions")
    public CompletableFuture<ResponseEntity<List<ReorderSuggestion>>> getReorderSuggestionsAsync() {
        return asyncMedicationService.generateReorderSuggestionsAsync().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/combined-report")
    public CompletableFuture<ResponseEntity<CombinedInventoryReport>> getCombinedReportAsync() {
        CompletableFuture<MedicationReport> reportFuture = asyncMedicationService.generateReportAsync();
        CompletableFuture<InventoryAlerts> alertsFuture = asyncMedicationService.getInventoryAlertsAsync();
        CompletableFuture<List<ReorderSuggestion>> reorderFuture = asyncMedicationService
                .generateReorderSuggestionsAsync();

        return CompletableFuture.allOf(reportFuture, alertsFuture, reorderFuture).thenApply(v -> {
            CombinedInventoryReport combined = new CombinedInventoryReport(reportFuture.join(), alertsFuture.join(),
                    reorderFuture.join());
            return ResponseEntity.ok(combined);
        });
    }

    public record CombinedInventoryReport(MedicationReport report, InventoryAlerts alerts,
            List<ReorderSuggestion> reorderSuggestions) {
    }
}
