package com.kaddy.controller.async;

import com.kaddy.dto.PatientDTO;
import com.kaddy.service.async.AsyncPatientService;
import com.kaddy.service.async.AsyncPatientService.PatientStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Async Patient Controller with reactive patterns
 */
@RestController
@RequestMapping("/api/async/patients")
@RequiredArgsConstructor
@Tag(name = "Async Patient Management", description = "Asynchronous APIs for patient operations with multi-threading")
public class AsyncPatientController {

    private final AsyncPatientService asyncPatientService;

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID asynchronously",
               description = "Retrieve a patient using non-blocking async operation")
    public CompletableFuture<ResponseEntity<PatientDTO>> getPatientByIdAsync(@PathVariable Long id) {
        return asyncPatientService.getPatientByIdAsync(id)
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Get all patients asynchronously",
               description = "Retrieve all patients with parallel processing")
    public CompletableFuture<ResponseEntity<List<PatientDTO>>> getAllPatientsAsync() {
        return asyncPatientService.getAllPatientsAsync()
            .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple patients asynchronously",
               description = "Bulk create patients with parallel processing")
    public CompletableFuture<ResponseEntity<List<PatientDTO>>> createPatientsAsync(
            @Valid @RequestBody List<PatientDTO> patientDTOs) {
        return asyncPatientService.createPatientsAsync(patientDTOs)
            .thenApply(patients -> ResponseEntity.status(201).body(patients));
    }

    @GetMapping("/search/advanced")
    @Operation(summary = "Advanced patient search",
               description = "Search patients with multiple criteria using async processing")
    public CompletableFuture<ResponseEntity<List<PatientDTO>>> searchPatientsAsync(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bloodGroup) {
        return asyncPatientService.searchPatientsAsync(name, bloodGroup)
            .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/batch")
    @Operation(summary = "Update multiple patients asynchronously",
               description = "Bulk update patients with concurrent processing")
    public CompletableFuture<ResponseEntity<List<PatientDTO>>> updatePatientsAsync(
            @Valid @RequestBody List<PatientDTO> patientDTOs) {
        return asyncPatientService.updatePatientsAsync(patientDTOs)
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get patient statistics",
               description = "Calculate comprehensive patient statistics using parallel processing")
    public CompletableFuture<ResponseEntity<PatientStatistics>> getStatisticsAsync() {
        return asyncPatientService.getPatientStatisticsAsync()
            .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/batch-process")
    @Operation(summary = "Batch process patients",
               description = "Apply bulk operations to multiple patients")
    public CompletableFuture<ResponseEntity<String>> batchProcessAsync(
            @RequestBody List<Long> patientIds,
            @RequestParam String operation) {

        java.util.function.Consumer<com.kaddy.model.Patient> operationFunc = switch (operation) {
            case "activate" -> patient -> patient.setActive(true);
            case "deactivate" -> patient -> patient.setActive(false);
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };

        return asyncPatientService.batchProcessPatientsAsync(patientIds, operationFunc)
            .thenApply(v -> ResponseEntity.ok("Batch processing completed for " + patientIds.size() + " patients"));
    }
}
