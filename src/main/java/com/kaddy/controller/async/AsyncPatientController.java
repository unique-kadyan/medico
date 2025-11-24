package com.kaddy.controller.async;

import com.kaddy.dto.PatientDTO;
import com.kaddy.service.async.AsyncPatientService;
import com.kaddy.service.async.AsyncPatientService.PatientStatistics;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/async/patients")
@RequiredArgsConstructor
public class AsyncPatientController {

    private final AsyncPatientService asyncPatientService;

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<PatientDTO>> getPatientByIdAsync(@PathVariable Long id) {
        return asyncPatientService.getPatientByIdAsync(id).thenApply(ResponseEntity::ok);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<PatientDTO>>> getAllPatientsAsync() {
        return asyncPatientService.getAllPatientsAsync().thenApply(ResponseEntity::ok);
    }

    @PostMapping("/batch")
    public CompletableFuture<ResponseEntity<List<PatientDTO>>> createPatientsAsync(
            @Valid @RequestBody List<PatientDTO> patientDTOs) {
        return asyncPatientService.createPatientsAsync(patientDTOs)
                .thenApply(patients -> ResponseEntity.status(201).body(patients));
    }

    @GetMapping("/search/advanced")
    public CompletableFuture<ResponseEntity<List<PatientDTO>>> searchPatientsAsync(
            @RequestParam(required = false) String name, @RequestParam(required = false) String bloodGroup) {
        return asyncPatientService.searchPatientsAsync(name, bloodGroup).thenApply(ResponseEntity::ok);
    }

    @PutMapping("/batch")
    public CompletableFuture<ResponseEntity<List<PatientDTO>>> updatePatientsAsync(
            @Valid @RequestBody List<PatientDTO> patientDTOs) {
        return asyncPatientService.updatePatientsAsync(patientDTOs).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/statistics")
    public CompletableFuture<ResponseEntity<PatientStatistics>> getStatisticsAsync() {
        return asyncPatientService.getPatientStatisticsAsync().thenApply(ResponseEntity::ok);
    }

    @PostMapping("/batch-process")
    public CompletableFuture<ResponseEntity<String>> batchProcessAsync(@RequestBody List<Long> patientIds,
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
