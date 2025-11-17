package com.kaddy.controller;

import com.kaddy.model.Appointment;
import com.kaddy.model.LabTest;
import com.kaddy.model.Patient;
import com.kaddy.model.Prescription;
import com.kaddy.repository.AppointmentRepository;
import com.kaddy.repository.LabTestRepository;
import com.kaddy.repository.PatientRepository;
import com.kaddy.repository.PrescriptionRepository;
import com.kaddy.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Patient Portal Controller
 * Allows patients to view their own medical information
 */
@RestController
@RequestMapping("/api/patient-portal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient Portal", description = "APIs for patients to access their own medical information")
public class PatientPortalController {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final LabTestRepository labTestRepository;
    private final PrescriptionRepository prescriptionRepository;

    @GetMapping("/my-profile")
    @Operation(summary = "Get patient's own profile", description = "Get the medical profile of the logged-in patient")
    public ResponseEntity<Patient> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Patient {} requesting own profile", username);

        var user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Patient patient = patientRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user: " + username));

        return ResponseEntity.ok(patient);
    }

    @GetMapping("/my-appointments")
    @Operation(summary = "Get patient's appointments", description = "Get all appointments for the logged-in patient")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        Patient patient = getCurrentPatient();
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(apt -> apt.getPatient() != null && apt.getPatient().getId().equals(patient.getId()))
                .toList();

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/my-lab-tests")
    @Operation(summary = "Get patient's lab tests", description = "Get all lab test results for the logged-in patient")
    public ResponseEntity<List<LabTest>> getMyLabTests() {
        Patient patient = getCurrentPatient();
        List<LabTest> labTests = labTestRepository.findAll().stream()
                .filter(test -> test.getPatient() != null && test.getPatient().getId().equals(patient.getId()))
                .toList();

        return ResponseEntity.ok(labTests);
    }

    @GetMapping("/my-prescriptions")
    @Operation(summary = "Get patient's prescriptions", description = "Get all prescriptions for the logged-in patient")
    public ResponseEntity<List<Prescription>> getMyPrescriptions() {
        Patient patient = getCurrentPatient();
        List<Prescription> prescriptions = prescriptionRepository.findAll().stream()
                .filter(rx -> rx.getPatient() != null && rx.getPatient().getId().equals(patient.getId()))
                .toList();

        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/my-medical-records")
    @Operation(summary = "Get patient's medical records", description = "Get all medical records for the logged-in patient")
    public ResponseEntity<?> getMyMedicalRecords() {
        Patient patient = getCurrentPatient();
        return ResponseEntity.ok(patient.getMedicalRecords());
    }

    /**
     * Helper method to get the current logged-in patient
     */
    private Patient getCurrentPatient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        var user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return patientRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user: " + username));
    }
}
