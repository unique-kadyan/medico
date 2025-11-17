package com.kaddy.controller;

import com.kaddy.dto.FollowUpDTO;
import com.kaddy.model.FollowUp;
import com.kaddy.service.FollowUpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/followups")
@RequiredArgsConstructor
@Tag(name = "Follow-up Management", description = "APIs for managing patient follow-up appointments")
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping
    @Operation(summary = "Get all follow-ups", description = "Retrieve a list of all follow-up appointments")
    public ResponseEntity<List<FollowUpDTO>> getAllFollowUps() {
        return ResponseEntity.ok(followUpService.getAllFollowUps());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get follow-up by ID", description = "Retrieve a specific follow-up appointment by its ID")
    public ResponseEntity<FollowUpDTO> getFollowUpById(@PathVariable Long id) {
        return ResponseEntity.ok(followUpService.getFollowUpById(id));
    }

    @GetMapping("/patient/{id}")
    @Operation(summary = "Get follow-ups by patient", description = "Retrieve all follow-up appointments for a specific patient")
    public ResponseEntity<List<FollowUpDTO>> getFollowUpsByPatient(@PathVariable Long id) {
        return ResponseEntity.ok(followUpService.getFollowUpsByPatient(id));
    }

    @GetMapping("/doctor/{id}")
    @Operation(summary = "Get follow-ups by doctor", description = "Retrieve all follow-up appointments for a specific doctor")
    public ResponseEntity<List<FollowUpDTO>> getFollowUpsByDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(followUpService.getFollowUpsByDoctor(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get follow-ups by status", description = "Retrieve all follow-up appointments with a specific status")
    public ResponseEntity<List<FollowUpDTO>> getFollowUpsByStatus(@PathVariable FollowUp.FollowUpStatus status) {
        return ResponseEntity.ok(followUpService.getFollowUpsByStatus(status));
    }

    @GetMapping("/patient/{id}/status/{status}")
    @Operation(summary = "Get follow-ups by patient and status", description = "Retrieve follow-up appointments for a patient with a specific status")
    public ResponseEntity<List<FollowUpDTO>> getFollowUpsByPatientAndStatus(
            @PathVariable Long id,
            @PathVariable FollowUp.FollowUpStatus status) {
        return ResponseEntity.ok(followUpService.getFollowUpsByPatientAndStatus(id, status));
    }

    @PostMapping
    @Operation(summary = "Schedule a follow-up", description = "Create a new follow-up appointment")
    public ResponseEntity<FollowUpDTO> scheduleFollowUp(@Valid @RequestBody FollowUpDTO followUpDTO) {
        FollowUpDTO createdFollowUp = followUpService.scheduleFollowUp(followUpDTO);
        return new ResponseEntity<>(createdFollowUp, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update follow-up", description = "Update an existing follow-up appointment")
    public ResponseEntity<FollowUpDTO> updateFollowUp(
            @PathVariable Long id,
            @Valid @RequestBody FollowUpDTO followUpDTO) {
        return ResponseEntity.ok(followUpService.updateFollowUp(id, followUpDTO));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete follow-up", description = "Mark a follow-up appointment as completed")
    public ResponseEntity<FollowUpDTO> completeFollowUp(@PathVariable Long id) {
        return ResponseEntity.ok(followUpService.completeFollowUp(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete follow-up", description = "Cancel a follow-up appointment")
    public ResponseEntity<Void> deleteFollowUp(@PathVariable Long id) {
        followUpService.deleteFollowUp(id);
        return ResponseEntity.noContent().build();
    }
}
