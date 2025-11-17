package com.kaddy.controller;

import com.kaddy.dto.MedicationRequestDTO;
import com.kaddy.model.enums.MedicationRequestStatus;
import com.kaddy.service.MedicationRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medication-requests")
@RequiredArgsConstructor
@Tag(name = "Medication Request Management", description = "APIs for managing medication requests and approvals")
public class MedicationRequestController {

    private final MedicationRequestService medicationRequestService;

    @PostMapping
    @Operation(summary = "Create medication request", description = "Create a new medication request for pharmacy approval")
    public ResponseEntity<MedicationRequestDTO> createRequest(@Valid @RequestBody MedicationRequestDTO requestDTO) {
        MedicationRequestDTO createdRequest = medicationRequestService.createRequest(requestDTO);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all medication requests", description = "Retrieve a list of all medication requests")
    public ResponseEntity<List<MedicationRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(medicationRequestService.getAllRequests());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medication request by ID", description = "Retrieve a specific medication request by its ID")
    public ResponseEntity<MedicationRequestDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationRequestService.getRequestById(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get medication requests by status", description = "Retrieve all medication requests with a specific status")
    public ResponseEntity<List<MedicationRequestDTO>> getRequestsByStatus(@PathVariable MedicationRequestStatus status) {
        return ResponseEntity.ok(medicationRequestService.getRequestsByStatus(status));
    }

    @GetMapping("/doctor/{id}")
    @Operation(summary = "Get medication requests by doctor", description = "Retrieve all medication requests submitted by a specific doctor")
    public ResponseEntity<List<MedicationRequestDTO>> getRequestsByDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(medicationRequestService.getRequestsByDoctor(id));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve medication request", description = "Approve a pending medication request (admin only)")
    public ResponseEntity<MedicationRequestDTO> approveRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestData) {

        Long reviewerId = Long.valueOf(requestData.get("reviewerId").toString());
        String reviewNotes = requestData.get("reviewNotes") != null ? requestData.get("reviewNotes").toString() : null;

        MedicationRequestDTO approvedRequest = medicationRequestService.approveRequest(id, reviewerId, reviewNotes);
        return ResponseEntity.ok(approvedRequest);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject medication request", description = "Reject a pending medication request (admin only)")
    public ResponseEntity<MedicationRequestDTO> rejectRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestData) {

        Long reviewerId = Long.valueOf(requestData.get("reviewerId").toString());
        String reviewNotes = requestData.get("reviewNotes") != null ? requestData.get("reviewNotes").toString() : null;

        MedicationRequestDTO rejectedRequest = medicationRequestService.rejectRequest(id, reviewerId, reviewNotes);
        return ResponseEntity.ok(rejectedRequest);
    }
}
