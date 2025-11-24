package com.kaddy.controller;

import com.kaddy.dto.EmergencyPatientDTO;
import com.kaddy.model.enums.PatientCondition;
import com.kaddy.service.EmergencyPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency-patients")
@RequiredArgsConstructor
public class EmergencyPatientController {

    private final EmergencyPatientService emergencyPatientService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyPatientDTO> admitPatient(@RequestBody EmergencyPatientDTO dto) {
        EmergencyPatientDTO admitted = emergencyPatientService.admitPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(admitted);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyPatientDTO>> getAllEmergencyPatients() {
        List<EmergencyPatientDTO> patients = emergencyPatientService.getAllEmergencyPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyPatientDTO>> getCurrentPatients() {
        List<EmergencyPatientDTO> patients = emergencyPatientService.getCurrentPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyPatientDTO>> getPatientsByRoom(@PathVariable Long roomId) {
        List<EmergencyPatientDTO> patients = emergencyPatientService.getPatientsByRoom(roomId);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/condition/{condition}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyPatientDTO>> getPatientsByCondition(@PathVariable PatientCondition condition) {
        List<EmergencyPatientDTO> patients = emergencyPatientService.getPatientsByCondition(condition);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/monitoring")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyPatientDTO>> getPatientsRequiringMonitoring() {
        List<EmergencyPatientDTO> patients = emergencyPatientService.getPatientsRequiringMonitoring();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyPatientDTO>> getPatientsByDoctor(@PathVariable Long doctorId) {
        List<EmergencyPatientDTO> patients = emergencyPatientService.getPatientsByDoctor(doctorId);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyPatientDTO>> getPatientHistory(@PathVariable Long patientId) {
        List<EmergencyPatientDTO> history = emergencyPatientService.getPatientHistory(patientId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyPatientDTO> getEmergencyPatientById(@PathVariable Long id) {
        EmergencyPatientDTO patient = emergencyPatientService.getEmergencyPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyPatientDTO> updateEmergencyPatient(@PathVariable Long id,
            @RequestBody EmergencyPatientDTO dto) {
        EmergencyPatientDTO updated = emergencyPatientService.updateEmergencyPatient(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/condition")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyPatientDTO> updateCondition(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        PatientCondition condition = PatientCondition.valueOf(body.get("condition"));
        EmergencyPatientDTO updated = emergencyPatientService.updateCondition(id, condition);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/discharge")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
    public ResponseEntity<EmergencyPatientDTO> dischargePatient(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String dischargeNotes = body != null ? body.get("dischargeNotes") : null;
        EmergencyPatientDTO discharged = emergencyPatientService.dischargePatient(id, dischargeNotes);
        return ResponseEntity.ok(discharged);
    }

    @PutMapping("/{id}/transfer")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyPatientDTO> transferToRoom(@PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long newRoomId = body.get("newRoomId");
        EmergencyPatientDTO transferred = emergencyPatientService.transferToRoom(id, newRoomId);
        return ResponseEntity.ok(transferred);
    }
}
