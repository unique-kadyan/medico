package com.kaddy.controller;

import com.kaddy.dto.MedicationDTO;
import com.kaddy.service.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
@Tag(name = "Medication Management", description = "APIs for managing pharmacy medication inventory")
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    @Operation(summary = "Get all medications", description = "Retrieve a list of all medications in inventory")
    public ResponseEntity<List<MedicationDTO>> getAllMedications() {
        return ResponseEntity.ok(medicationService.getAllMedications());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active medications", description = "Retrieve a list of all active medications")
    public ResponseEntity<List<MedicationDTO>> getAllActiveMedications() {
        return ResponseEntity.ok(medicationService.getAllActiveMedications());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medication by ID", description = "Retrieve a specific medication by its ID")
    public ResponseEntity<MedicationDTO> getMedicationById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getMedicationById(id));
    }

    @GetMapping("/code/{medicationCode}")
    @Operation(summary = "Get medication by code", description = "Retrieve a specific medication by its code")
    public ResponseEntity<MedicationDTO> getMedicationByCode(@PathVariable String medicationCode) {
        return ResponseEntity.ok(medicationService.getMedicationByCode(medicationCode));
    }

    @GetMapping("/search")
    @Operation(summary = "Search medications by name", description = "Search for medications by name")
    public ResponseEntity<List<MedicationDTO>> searchMedicationsByName(@RequestParam String name) {
        return ResponseEntity.ok(medicationService.searchMedicationsByName(name));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get medications by category", description = "Retrieve medications by category")
    public ResponseEntity<List<MedicationDTO>> getMedicationsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(medicationService.getMedicationsByCategory(category));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock medications", description = "Retrieve medications with stock below reorder level")
    public ResponseEntity<List<MedicationDTO>> getLowStockMedications() {
        return ResponseEntity.ok(medicationService.getLowStockMedications());
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired medications", description = "Retrieve medications that have expired")
    public ResponseEntity<List<MedicationDTO>> getExpiredMedications() {
        return ResponseEntity.ok(medicationService.getExpiredMedications());
    }

    @GetMapping("/expiring-soon")
    @Operation(summary = "Get medications expiring soon", description = "Retrieve medications expiring within 3 months")
    public ResponseEntity<List<MedicationDTO>> getExpiringSoonMedications() {
        return ResponseEntity.ok(medicationService.getExpiringSoonMedications());
    }

    @PostMapping
    @Operation(summary = "Create a new medication", description = "Add a new medication to the inventory")
    public ResponseEntity<MedicationDTO> createMedication(@Valid @RequestBody MedicationDTO medicationDTO) {
        MedicationDTO createdMedication = medicationService.createMedication(medicationDTO);
        return new ResponseEntity<>(createdMedication, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medication", description = "Update an existing medication's information")
    public ResponseEntity<MedicationDTO> updateMedication(
            @PathVariable Long id,
            @Valid @RequestBody MedicationDTO medicationDTO) {
        return ResponseEntity.ok(medicationService.updateMedication(id, medicationDTO));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update medication stock", description = "Update stock quantity for a medication")
    public ResponseEntity<MedicationDTO> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(medicationService.updateStock(id, quantity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medication", description = "Deactivate a medication (soft delete)")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
        return ResponseEntity.noContent().build();
    }
}
