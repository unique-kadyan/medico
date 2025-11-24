package com.kaddy.service.async;

import com.kaddy.dto.MedicationDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.functional.FunctionalUtils;
import com.kaddy.model.Medication;
import com.kaddy.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncMedicationService {

    private final MedicationRepository medicationRepository;
    private final ModelMapper modelMapper;
    private final Executor taskExecutor;
    private final Executor batchExecutor;

    @Async("taskExecutor")
    public CompletableFuture<List<MedicationDTO>> getAllMedicationsAsync() {
        log.info("Async: Fetching all medications");

        return CompletableFuture.supplyAsync(() -> medicationRepository.findAll().parallelStream()
                .map(this::convertToDTO).collect(Collectors.toList()), taskExecutor);
    }

    @Async("taskExecutor")
    public CompletableFuture<InventoryAlerts> getInventoryAlertsAsync() {
        log.info("Async: Calculating inventory alerts");

        return CompletableFuture.supplyAsync(() -> {
            List<Medication> allMedications = medicationRepository.findAll();

            CompletableFuture<List<MedicationDTO>> lowStockFuture = CompletableFuture
                    .supplyAsync(() -> filterMedications(allMedications, Medication::isLowStock), taskExecutor);

            CompletableFuture<List<MedicationDTO>> expiredFuture = CompletableFuture
                    .supplyAsync(() -> filterMedications(allMedications, Medication::isExpired), taskExecutor);

            CompletableFuture<List<MedicationDTO>> expiringSoonFuture = CompletableFuture
                    .supplyAsync(() -> filterMedications(allMedications, Medication::isExpiringSoon), taskExecutor);

            CompletableFuture.allOf(lowStockFuture, expiredFuture, expiringSoonFuture).join();

            return new InventoryAlerts(lowStockFuture.join(), expiredFuture.join(), expiringSoonFuture.join());
        }, taskExecutor);
    }

    @Async("batchExecutor")
    @Transactional
    public CompletableFuture<List<MedicationDTO>> batchUpdateStockAsync(Map<Long, Integer> stockUpdates) {

        log.info("Async: Batch updating stock for {} medications", stockUpdates.size());

        return CompletableFuture.supplyAsync(() -> {
            List<CompletableFuture<MedicationDTO>> updateFutures = stockUpdates.entrySet().stream()
                    .map(entry -> updateStockAsync(entry.getKey(), entry.getValue())).collect(Collectors.toList());

            return FunctionalUtils.waitAll(updateFutures).join();
        }, batchExecutor);
    }

    private CompletableFuture<MedicationDTO> updateStockAsync(Long id, Integer quantity) {
        return CompletableFuture.supplyAsync(() -> {
            Medication medication = medicationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Medication not found with ID: " + id));

            medication.setStockQuantity(medication.getStockQuantity() + quantity);
            Medication updated = medicationRepository.save(medication);

            log.debug("Stock updated for medication {}: {} -> {}", id, medication.getStockQuantity() - quantity,
                    medication.getStockQuantity());

            return convertToDTO(updated);
        }, taskExecutor);
    }

    @Async("reportExecutor")
    public CompletableFuture<MedicationReport> generateReportAsync() {
        log.info("Async: Generating medication report");

        return CompletableFuture.supplyAsync(() -> {
            List<Medication> allMedications = medicationRepository.findAll();

            CompletableFuture<Long> totalMedicationsFuture = CompletableFuture
                    .supplyAsync(() -> (long) allMedications.size(), taskExecutor);

            CompletableFuture<Long> activeMedicationsFuture = CompletableFuture.supplyAsync(
                    () -> allMedications.parallelStream().filter(Medication::getActive).count(), taskExecutor);

            CompletableFuture<Integer> totalStockFuture = CompletableFuture.supplyAsync(
                    () -> allMedications.parallelStream().mapToInt(Medication::getStockQuantity).sum(), taskExecutor);

            CompletableFuture<Map<String, Long>> categoryDistFuture = CompletableFuture.supplyAsync(
                    () -> allMedications.parallelStream()
                            .collect(Collectors.groupingByConcurrent(Medication::getCategory, Collectors.counting())),
                    taskExecutor);

            CompletableFuture<Long> lowStockCountFuture = CompletableFuture.supplyAsync(
                    () -> allMedications.parallelStream().filter(Medication::isLowStock).count(), taskExecutor);

            CompletableFuture<Long> expiredCountFuture = CompletableFuture.supplyAsync(
                    () -> allMedications.parallelStream().filter(Medication::isExpired).count(), taskExecutor);

            CompletableFuture.allOf(totalMedicationsFuture, activeMedicationsFuture, totalStockFuture,
                    categoryDistFuture, lowStockCountFuture, expiredCountFuture).join();

            return new MedicationReport(totalMedicationsFuture.join(), activeMedicationsFuture.join(),
                    totalStockFuture.join(), categoryDistFuture.join(), lowStockCountFuture.join(),
                    expiredCountFuture.join(), LocalDate.now());
        }, taskExecutor);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<MedicationDTO>> advancedSearchAsync(String name, String category, Boolean lowStock,
            Boolean expired) {

        log.info("Async: Advanced search - name: {}, category: {}, lowStock: {}, expired: {}", name, category, lowStock,
                expired);

        return CompletableFuture.supplyAsync(() -> {
            List<Medication> allMedications = medicationRepository.findAll();

            Predicate<Medication> combinedFilter = med -> true;

            if (name != null && !name.isEmpty()) {
                combinedFilter = combinedFilter.and(med -> med.getName().toLowerCase().contains(name.toLowerCase()));
            }

            if (category != null && !category.isEmpty()) {
                combinedFilter = combinedFilter.and(med -> med.getCategory().equalsIgnoreCase(category));
            }

            if (Boolean.TRUE.equals(lowStock)) {
                combinedFilter = combinedFilter.and(Medication::isLowStock);
            }

            if (Boolean.TRUE.equals(expired)) {
                combinedFilter = combinedFilter.and(Medication::isExpired);
            }

            return FunctionalUtils.filterAndTransform(allMedications, combinedFilter, this::convertToDTO);
        }, taskExecutor);
    }

    @Async("batchExecutor")
    @Transactional
    public CompletableFuture<BulkImportResult> bulkImportMedicationsAsync(List<MedicationDTO> medicationDTOs) {

        log.info("Async: Bulk importing {} medications", medicationDTOs.size());

        return CompletableFuture.supplyAsync(() -> {
            int successCount = 0;
            int failureCount = 0;
            List<String> errors = new java.util.ArrayList<>();

            List<List<MedicationDTO>> batches = FunctionalUtils.partitionList(medicationDTOs, 50);

            for (List<MedicationDTO> batch : batches) {
                try {
                    List<Medication> medications = batch.parallelStream().map(dto -> {
                        if (medicationRepository.existsByMedicationCode(dto.getMedicationCode())) {
                            errors.add("Medication code already exists: " + dto.getMedicationCode());
                            return null;
                        }
                        return convertToEntity(dto);
                    }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

                    List<Medication> saved = medicationRepository.saveAll(medications);
                    successCount += saved.size();

                } catch (Exception e) {
                    failureCount += batch.size();
                    errors.add("Batch processing failed: " + e.getMessage());
                    log.error("Error processing batch", e);
                }
            }

            log.info("Bulk import completed: {} successful, {} failed", successCount, failureCount);

            return new BulkImportResult(successCount, failureCount, errors);
        }, batchExecutor);
    }

    @Async("batchExecutor")
    public CompletableFuture<List<ReorderSuggestion>> generateReorderSuggestionsAsync() {
        log.info("Async: Generating reorder suggestions");

        return CompletableFuture.supplyAsync(() -> {
            List<Medication> lowStockMeds = medicationRepository.findLowStockMedications();

            return lowStockMeds.parallelStream()
                    .map(med -> new ReorderSuggestion(med.getId(), med.getName(), med.getMedicationCode(),
                            med.getStockQuantity(), med.getReorderLevel(), med.getReorderQuantity(),
                            med.getUnitPrice().multiply(java.math.BigDecimal.valueOf(med.getReorderQuantity()))))
                    .collect(Collectors.toList());
        }, taskExecutor);
    }

    private List<MedicationDTO> filterMedications(List<Medication> medications, Predicate<Medication> filter) {
        return medications.parallelStream().filter(filter).map(this::convertToDTO).collect(Collectors.toList());
    }

    private MedicationDTO convertToDTO(Medication medication) {
        MedicationDTO dto = modelMapper.map(medication, MedicationDTO.class);
        dto.setLowStock(medication.isLowStock());
        dto.setExpired(medication.isExpired());
        dto.setExpiringSoon(medication.isExpiringSoon());
        return dto;
    }

    private Medication convertToEntity(MedicationDTO dto) {
        return modelMapper.map(dto, Medication.class);
    }

    public record InventoryAlerts(List<MedicationDTO> lowStock, List<MedicationDTO> expired,
            List<MedicationDTO> expiringSoon) {
    }

    public record MedicationReport(long totalMedications, long activeMedications, int totalStockQuantity,
            Map<String, Long> categoryDistribution, long lowStockCount, long expiredCount, LocalDate generatedDate) {
    }

    public record BulkImportResult(int successCount, int failureCount, List<String> errors) {
    }

    public record ReorderSuggestion(Long medicationId, String medicationName, String medicationCode, int currentStock,
            int reorderLevel, int suggestedQuantity, java.math.BigDecimal estimatedCost) {
    }
}
