package com.kaddy.controller;

import com.kaddy.dto.PrescriptionDTO;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<PrescriptionDTO>> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDTO> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    @GetMapping("/number/{prescriptionNumber}")
    public ResponseEntity<PrescriptionDTO> getPrescriptionByNumber(@PathVariable String prescriptionNumber) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionByNumber(prescriptionNumber));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatientId(patientId));
    }

    @GetMapping("/patient/{patientId}/undispensed")
    public ResponseEntity<List<PrescriptionDTO>> getUndispensedByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getUndispensedPrescriptionsByPatientId(patientId));
    }

    @GetMapping("/undispensed")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<List<PrescriptionDTO>> getUndispensedPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getUndispensedPrescriptions());
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByDoctorId(doctorId));
    }

    @PutMapping("/{id}/dispense")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<PrescriptionDTO> dispensePrescription(@PathVariable Long id) {
        Long dispensedById = securityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return ResponseEntity.ok(prescriptionService.dispensePrescription(id, dispensedById));
    }
}
