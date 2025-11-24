package com.kaddy.controller;

import com.kaddy.dto.MedicationDTO;
import com.kaddy.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    public ResponseEntity<List<MedicationDTO>> getAllMedications() {
        return ResponseEntity.ok(medicationService.getAllMedications());
    }

    @GetMapping("/active")
    public ResponseEntity<List<MedicationDTO>> getAllActiveMedications() {
        return ResponseEntity.ok(medicationService.getAllActiveMedications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationDTO> getMedicationById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getMedicationById(id));
    }

    @GetMapping("/code/{medicationCode}")
    public ResponseEntity<MedicationDTO> getMedicationByCode(@PathVariable String medicationCode) {
        return ResponseEntity.ok(medicationService.getMedicationByCode(medicationCode));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicationDTO>> searchMedicationsByName(@RequestParam String name) {
        return ResponseEntity.ok(medicationService.searchMedicationsByName(name));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<MedicationDTO>> getMedicationsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(medicationService.getMedicationsByCategory(category));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<MedicationDTO>> getLowStockMedications() {
        return ResponseEntity.ok(medicationService.getLowStockMedications());
    }

    @GetMapping("/expired")
    public ResponseEntity<List<MedicationDTO>> getExpiredMedications() {
        return ResponseEntity.ok(medicationService.getExpiredMedications());
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<MedicationDTO>> getExpiringSoonMedications() {
        return ResponseEntity.ok(medicationService.getExpiringSoonMedications());
    }

    @PostMapping
    public ResponseEntity<MedicationDTO> createMedication(@Valid @RequestBody MedicationDTO medicationDTO) {
        MedicationDTO createdMedication = medicationService.createMedication(medicationDTO);
        return new ResponseEntity<>(createdMedication, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationDTO> updateMedication(@PathVariable Long id,
            @Valid @RequestBody MedicationDTO medicationDTO) {
        return ResponseEntity.ok(medicationService.updateMedication(id, medicationDTO));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<MedicationDTO> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(medicationService.updateStock(id, quantity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
        return ResponseEntity.noContent().build();
    }
}
