package com.kaddy.controller;

import com.kaddy.dto.AppointmentDTO;
import com.kaddy.dto.AppointmentRequest;
import com.kaddy.model.enums.AppointmentStatus;
import com.kaddy.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointment Management", description = "APIs for managing appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "Get all appointments", description = "Get all appointments (filtered by role)")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID", description = "Get appointment details by ID")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get appointments by patient", description = "Get all appointments for a specific patient")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get appointments by doctor", description = "Get all appointments for a specific doctor")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's appointments", description = "Get all appointments scheduled for today")
    public ResponseEntity<List<AppointmentDTO>> getTodaysAppointments() {
        List<AppointmentDTO> appointments = appointmentService.getTodaysAppointments();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming appointments", description = "Get all upcoming appointments")
    public ResponseEntity<List<AppointmentDTO>> getUpcomingAppointments() {
        List<AppointmentDTO> appointments = appointmentService.getUpcomingAppointments();
        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    @Operation(summary = "Create appointment", description = "Create a new appointment")
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentDTO appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update appointment", description = "Update an existing appointment")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentDTO appointment = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(appointment);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update appointment status", description = "Update the status of an appointment")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        AppointmentDTO appointment = appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(appointment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete appointment", description = "Delete/cancel an appointment")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
