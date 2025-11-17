package com.kaddy.controller;

import com.kaddy.dto.NursePatientAssignmentDTO;
import com.kaddy.service.NursePatientAssignmentService;
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
@RequestMapping("/api/nurse-assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Nurse-Patient Assignment", description = "APIs for managing nurse-patient assignments")
public class NursePatientAssignmentController {

    private final NursePatientAssignmentService assignmentService;

    @PostMapping
    @Operation(summary = "Assign nurse to patient", description = "Create a new nurse-patient assignment")
    public ResponseEntity<NursePatientAssignmentDTO> assignNurseToPatient(
            @Valid @RequestBody NursePatientAssignmentDTO assignmentDTO) {
        log.info("REST request to assign nurse {} to patient {}",
                assignmentDTO.getNurseId(), assignmentDTO.getPatientId());
        NursePatientAssignmentDTO result = assignmentService.assignNurseToPatient(assignmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @Operation(summary = "Get all nurse-patient assignments", description = "Retrieve all nurse-patient assignments")
    public ResponseEntity<List<NursePatientAssignmentDTO>> getAllAssignments() {
        log.info("REST request to get all nurse-patient assignments");
        List<NursePatientAssignmentDTO> assignments = assignmentService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID", description = "Retrieve a nurse-patient assignment by ID")
    public ResponseEntity<NursePatientAssignmentDTO> getAssignmentById(@PathVariable Long id) {
        log.info("REST request to get assignment with id: {}", id);
        NursePatientAssignmentDTO assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/nurse/{nurseId}")
    @Operation(summary = "Get assignments by nurse", description = "Retrieve all assignments for a specific nurse")
    public ResponseEntity<List<NursePatientAssignmentDTO>> getAssignmentsByNurse(
            @PathVariable Long nurseId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("REST request to get assignments for nurse: {} (activeOnly: {})", nurseId, activeOnly);
        List<NursePatientAssignmentDTO> assignments = activeOnly
            ? assignmentService.getActiveAssignmentsByNurse(nurseId)
            : assignmentService.getAssignmentsByNurse(nurseId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get assignments by patient", description = "Retrieve all assignments for a specific patient")
    public ResponseEntity<List<NursePatientAssignmentDTO>> getAssignmentsByPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("REST request to get assignments for patient: {} (activeOnly: {})", patientId, activeOnly);
        List<NursePatientAssignmentDTO> assignments = activeOnly
            ? assignmentService.getActiveAssignmentsByPatient(patientId)
            : assignmentService.getAssignmentsByPatient(patientId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/nurse/{nurseId}/patient-count")
    @Operation(summary = "Get patient count for nurse", description = "Get the number of active patients assigned to a nurse")
    public ResponseEntity<Long> getPatientCountForNurse(@PathVariable Long nurseId) {
        log.info("REST request to get patient count for nurse: {}", nurseId);
        long count = assignmentService.getActivePatientCountForNurse(nurseId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate assignment", description = "Deactivate a nurse-patient assignment")
    public ResponseEntity<Void> deactivateAssignment(@PathVariable Long id) {
        log.info("REST request to deactivate assignment: {}", id);
        assignmentService.deactivateAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete assignment", description = "Delete a nurse-patient assignment")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        log.info("REST request to delete assignment: {}", id);
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
