package com.kaddy.controller;

import com.kaddy.dto.DoctorPatientAssignmentDTO;
import com.kaddy.service.DoctorPatientAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class DoctorPatientAssignmentController {

    private final DoctorPatientAssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<DoctorPatientAssignmentDTO> assignDoctorToPatient(
            @Valid @RequestBody DoctorPatientAssignmentDTO assignmentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.assignDoctorToPatient(assignmentDTO));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorPatientAssignmentDTO>> getDoctorAssignments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(assignmentService.getDoctorAssignments(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DoctorPatientAssignmentDTO>> getPatientAssignments(@PathVariable Long patientId) {
        return ResponseEntity.ok(assignmentService.getPatientAssignments(patientId));
    }

    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<Long> getDoctorPatientCount(@PathVariable Long doctorId) {
        return ResponseEntity.ok(assignmentService.getPatientCountForDoctor(doctorId));
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long assignmentId) {
        assignmentService.removeAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
