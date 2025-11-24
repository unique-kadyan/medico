package com.kaddy.controller;

import com.kaddy.dto.OTRequestDTO;
import com.kaddy.model.enums.OTRequestStatus;
import com.kaddy.service.OTRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ot-requests")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OTRequestController {

    private final OTRequestService otRequestService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
    public ResponseEntity<OTRequestDTO> createOTRequest(@RequestBody OTRequestDTO otRequestDTO) {
        OTRequestDTO created = otRequestService.createOTRequest(otRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<OTRequestDTO>> getAllOTRequests() {
        List<OTRequestDTO> requests = otRequestService.getAllOTRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<OTRequestDTO> getOTRequestById(@PathVariable Long id) {
        OTRequestDTO request = otRequestService.getOTRequestById(id);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/surgeon/{surgeonId}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<OTRequestDTO>> getOTRequestsBySurgeon(@PathVariable Long surgeonId) {
        List<OTRequestDTO> requests = otRequestService.getOTRequestsBySurgeon(surgeonId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<OTRequestDTO>> getOTRequestsByPatient(@PathVariable Long patientId) {
        List<OTRequestDTO> requests = otRequestService.getOTRequestsByPatient(patientId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<OTRequestDTO>> getOTRequestsByStatus(@PathVariable OTRequestStatus status) {
        List<OTRequestDTO> requests = otRequestService.getOTRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<OTRequestDTO>> getPendingOTRequests() {
        List<OTRequestDTO> requests = otRequestService.getPendingOTRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/emergency-pending")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<OTRequestDTO>> getEmergencyPendingRequests() {
        List<OTRequestDTO> requests = otRequestService.getEmergencyPendingRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<OTRequestDTO>> getOTRequestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<OTRequestDTO> requests = otRequestService.getOTRequestsByDateRange(startDate, endDate);
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
    public ResponseEntity<OTRequestDTO> updateOTRequest(@PathVariable Long id, @RequestBody OTRequestDTO otRequestDTO) {
        OTRequestDTO updated = otRequestService.updateOTRequest(id, otRequestDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'NURSE')")
    public ResponseEntity<OTRequestDTO> approveOTRequest(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        OTRequestDTO approved = otRequestService.approveOTRequest(id, notes);
        return ResponseEntity.ok(approved);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'NURSE')")
    public ResponseEntity<OTRequestDTO> rejectOTRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("rejectionReason");
        OTRequestDTO rejected = otRequestService.rejectOTRequest(id, reason);
        return ResponseEntity.ok(rejected);
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<OTRequestDTO> startSurgery(@PathVariable Long id) {
        OTRequestDTO started = otRequestService.startSurgery(id);
        return ResponseEntity.ok(started);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
    public ResponseEntity<OTRequestDTO> completeSurgery(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("postOperativeNotes") : null;
        OTRequestDTO completed = otRequestService.completeSurgery(id, notes);
        return ResponseEntity.ok(completed);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
    public ResponseEntity<OTRequestDTO> cancelOTRequest(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        OTRequestDTO cancelled = otRequestService.cancelOTRequest(id, reason);
        return ResponseEntity.ok(cancelled);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteOTRequest(@PathVariable Long id) {
        otRequestService.deleteOTRequest(id);
        return ResponseEntity.noContent().build();
    }
}
