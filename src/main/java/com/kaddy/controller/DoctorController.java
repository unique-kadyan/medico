package com.kaddy.controller;

import com.kaddy.dto.DoctorDTO;
import com.kaddy.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DoctorDTO>> getAllActiveDoctors() {
        return ResponseEntity.ok(doctorService.getAllActiveDoctors());
    }

    @GetMapping("/available")
    public ResponseEntity<List<DoctorDTO>> getAvailableDoctors() {
        return ResponseEntity.ok(doctorService.getAvailableDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/doctor-id/{doctorId}")
    public ResponseEntity<DoctorDTO> getDoctorByDoctorId(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorByDoctorId(doctorId));
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsBySpecialization(@PathVariable String specialization) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specialization));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<DoctorDTO>> getDoctorsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(doctorService.getDoctorsByDepartment(department));
    }

    @PostMapping
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @RequestBody DoctorDTO doctorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(doctorDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorDTO doctorDTO) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, doctorDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<DoctorDTO> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        return ResponseEntity.ok(doctorService.updateAvailability(id, available));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<DoctorDTO> activateDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.activateDoctor(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<DoctorDTO> deactivateDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.deactivateDoctor(id));
    }
}
