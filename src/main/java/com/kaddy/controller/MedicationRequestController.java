package com.kaddy.controller;

import com.kaddy.dto.MedicationRequestDTO;
import com.kaddy.model.User;
import com.kaddy.model.enums.MedicationRequestStatus;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.MedicationRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medication-requests")
@RequiredArgsConstructor
@Slf4j
public class MedicationRequestController {

    private final MedicationRequestService medicationRequestService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'DOCTOR_SUPERVISOR', 'ADMIN')")
    public ResponseEntity<MedicationRequestDTO> createRequest(@Valid @RequestBody MedicationRequestDTO requestDTO) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        requestDTO.setRequestedById(currentUser.getId());
        log.info("Doctor {} creating medication request for: {}", currentUser.getId(), requestDTO.getMedicationName());

        MedicationRequestDTO createdRequest = medicationRequestService.createRequest(requestDTO);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'DOCTOR_SUPERVISOR')")
    public ResponseEntity<List<MedicationRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(medicationRequestService.getAllRequests());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'DOCTOR_SUPERVISOR')")
    public ResponseEntity<MedicationRequestDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationRequestService.getRequestById(id));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'DOCTOR_SUPERVISOR')")
    public ResponseEntity<List<MedicationRequestDTO>> getRequestsByStatus(
            @PathVariable MedicationRequestStatus status) {
        return ResponseEntity.ok(medicationRequestService.getRequestsByStatus(status));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<MedicationRequestDTO>> getPendingRequests() {
        log.info("Fetching pending medication requests for approval");
        return ResponseEntity.ok(medicationRequestService.getRequestsByStatus(MedicationRequestStatus.PENDING));
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('DOCTOR', 'DOCTOR_SUPERVISOR', 'ADMIN')")
    public ResponseEntity<List<MedicationRequestDTO>> getMyRequests() {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return ResponseEntity.ok(medicationRequestService.getRequestsByDoctor(currentUser.getId()));
    }

    @GetMapping("/doctor/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR_SUPERVISOR')")
    public ResponseEntity<List<MedicationRequestDTO>> getRequestsByDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(medicationRequestService.getRequestsByDoctor(id));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<MedicationRequestDTO> approveRequest(@PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> requestData) {

        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        String reviewNotes = null;
        if (requestData != null && requestData.get("reviewNotes") != null) {
            reviewNotes = requestData.get("reviewNotes").toString();
        }

        log.info("User {} (role: {}) approving medication request {}", currentUser.getId(), currentUser.getRole(), id);

        MedicationRequestDTO approvedRequest = medicationRequestService.approveRequest(id, currentUser.getId(),
                reviewNotes);
        return ResponseEntity.ok(approvedRequest);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<MedicationRequestDTO> rejectRequest(@PathVariable Long id,
            @RequestBody Map<String, Object> requestData) {

        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        String reviewNotes = requestData.get("reviewNotes") != null ? requestData.get("reviewNotes").toString()
                : "No reason provided";

        log.info("User {} (role: {}) rejecting medication request {}", currentUser.getId(), currentUser.getRole(), id);

        MedicationRequestDTO rejectedRequest = medicationRequestService.rejectRequest(id, currentUser.getId(),
                reviewNotes);
        return ResponseEntity.ok(rejectedRequest);
    }
}
