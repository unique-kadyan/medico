package com.kaddy.controller;

import com.kaddy.dto.consent.CreateShareRequestDTO;
import com.kaddy.dto.consent.RecordShareRequestDTO;
import com.kaddy.dto.consent.SharedMedicalRecordDTO;
import com.kaddy.model.User;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.RecordSharingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/record-sharing")
@RequiredArgsConstructor
@Slf4j
public class RecordSharingController {

    private final RecordSharingService recordSharingService;
    private final SecurityUtils securityUtils;

    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<RecordShareRequestDTO> createShareRequest(@Valid @RequestBody CreateShareRequestDTO request) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital() != null ? currentUser.getHospital().getId() : null;
        if (hospitalId == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }

        log.info("Creating share request for patient from hospital {} by user {}", request.getSourceHospitalId(),
                currentUser.getId());

        RecordShareRequestDTO shareRequest = recordSharingService.createShareRequest(request, hospitalId);

        return new ResponseEntity<>(shareRequest, HttpStatus.CREATED);
    }

    @PostMapping("/request/{requestId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<RecordShareRequestDTO> approveShareRequest(@PathVariable Long requestId,
            @RequestParam(required = false) String responseNotes) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        log.info("Approving share request {} by user {}", requestId, currentUser.getId());

        RecordShareRequestDTO shareRequest = recordSharingService.approveShareRequest(requestId, currentUser.getId(),
                responseNotes);

        return ResponseEntity.ok(shareRequest);
    }

    @PostMapping("/request/{requestId}/deny")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<RecordShareRequestDTO> denyShareRequest(@PathVariable Long requestId,
            @RequestParam String reason) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        log.info("Denying share request {} by user {}", requestId, currentUser.getId());

        RecordShareRequestDTO shareRequest = recordSharingService.denyShareRequest(requestId, currentUser.getId(),
                reason);

        return ResponseEntity.ok(shareRequest);
    }

    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<RecordShareRequestDTO> getShareRequest(@PathVariable Long requestId) {
        log.info("Getting share request by ID: {}", requestId);
        return ResponseEntity.ok(recordSharingService.getShareRequest(requestId));
    }

    @GetMapping("/request/number/{requestNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<RecordShareRequestDTO> getShareRequestByNumber(@PathVariable String requestNumber) {
        log.info("Getting share request by number: {}", requestNumber);
        return ResponseEntity.ok(recordSharingService.getShareRequestByNumber(requestNumber));
    }

    @GetMapping("/request/{requestId}/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<SharedMedicalRecordDTO> getSharedRecords(@PathVariable Long requestId) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        log.info("Getting shared records for request {} by user {}", requestId, currentUser.getId());

        SharedMedicalRecordDTO sharedRecords = recordSharingService.getSharedRecords(requestId, currentUser.getId());

        return ResponseEntity.ok(sharedRecords);
    }

    @GetMapping("/incoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Page<RecordShareRequestDTO>> getIncomingRequests(
            @PageableDefault(size = 20) Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital() != null ? currentUser.getHospital().getId() : null;
        if (hospitalId == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }

        log.info("Getting incoming requests for hospital: {}", hospitalId);
        return ResponseEntity.ok(recordSharingService.getIncomingRequests(hospitalId, pageable));
    }

    @GetMapping("/outgoing")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Page<RecordShareRequestDTO>> getOutgoingRequests(
            @PageableDefault(size = 20) Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital() != null ? currentUser.getHospital().getId() : null;
        if (hospitalId == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }

        log.info("Getting outgoing requests from hospital: {}", hospitalId);
        return ResponseEntity.ok(recordSharingService.getOutgoingRequests(hospitalId, pageable));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<RecordShareRequestDTO>> getPendingRequests() {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital() != null ? currentUser.getHospital().getId() : null;
        if (hospitalId == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }

        log.info("Getting pending requests for hospital: {}", hospitalId);
        return ResponseEntity.ok(recordSharingService.getPendingRequests(hospitalId));
    }
}
