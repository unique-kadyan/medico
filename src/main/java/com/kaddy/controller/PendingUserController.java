package com.kaddy.controller;

import com.kaddy.dto.DocumentDTO;
import com.kaddy.dto.PendingUserDTO;
import com.kaddy.model.Document;
import com.kaddy.model.PendingUser;
import com.kaddy.model.User;
import com.kaddy.model.enums.UserRole;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.PendingUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pending-users")
@RequiredArgsConstructor
public class PendingUserController {

    private final PendingUserService pendingUserService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PENDING_REGISTRATIONS')")
    public ResponseEntity<List<PendingUserDTO>> getAllPendingUsers() {
        List<PendingUser> pendingUsers = pendingUserService.getAllPendingUsers();
        List<PendingUserDTO> dtos = pendingUsers.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAuthority('VIEW_PENDING_REGISTRATIONS')")
    public ResponseEntity<List<PendingUserDTO>> getPendingUsersByRole(@PathVariable UserRole role) {
        List<PendingUser> pendingUsers = pendingUserService.getPendingUsersByRole(role);
        List<PendingUserDTO> dtos = pendingUsers.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_PENDING_REGISTRATIONS')")
    public ResponseEntity<PendingUserDTO> getPendingUserById(@PathVariable Long id) {
        PendingUser pendingUser = pendingUserService.getPendingUserById(id);
        return ResponseEntity.ok(convertToDTO(pendingUser));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('APPROVE_DOCTOR_REGISTRATION') or hasAuthority('APPROVE_NURSE_REGISTRATION') or hasAuthority('APPROVE_RECEPTIONIST_REGISTRATION') or hasAuthority('APPROVE_SUPERVISOR_REGISTRATION')")
    public ResponseEntity<?> approveRegistration(@PathVariable Long id) {
        try {
            Long currentUserId = securityUtils.getCurrentUserId()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
            User currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));

            PendingUser pendingUser = pendingUserService.getPendingUserById(id);
            if (!pendingUserService.canApprove(currentUser.getRole(), pendingUser.getRequestedRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You don't have permission to approve this role"));
            }

            User approvedUser = pendingUserService.approveRegistration(id, currentUserId);
            return ResponseEntity
                    .ok(Map.of("message", "Registration approved successfully", "userId", approvedUser.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('REJECT_REGISTRATION')")
    public ResponseEntity<?> rejectRegistration(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Long currentUserId = securityUtils.getCurrentUserId()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
            String reason = request.get("reason");

            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Rejection reason is required"));
            }

            pendingUserService.rejectRegistration(id, currentUserId, reason);
            return ResponseEntity.ok(Map.of("message", "Registration rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('VIEW_PENDING_REGISTRATIONS')")
    public ResponseEntity<List<DocumentDTO>> getDocuments(@PathVariable Long id) {
        List<Document> documents = pendingUserService.getDocumentsForPendingUser(id);
        List<DocumentDTO> dtos = documents.stream().map(this::convertDocumentToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private PendingUserDTO convertToDTO(PendingUser pendingUser) {
        PendingUserDTO dto = new PendingUserDTO();
        dto.setId(pendingUser.getId());
        dto.setUsername(pendingUser.getUsername());
        dto.setEmail(pendingUser.getEmail());
        dto.setFirstName(pendingUser.getFirstName());
        dto.setLastName(pendingUser.getLastName());
        dto.setPhone(pendingUser.getPhone());
        dto.setRequestedRole(pendingUser.getRequestedRole());
        dto.setStatus(pendingUser.getStatus());
        dto.setRequestedBy(pendingUser.getRequestedBy());
        dto.setApprovedBy(pendingUser.getApprovedBy());
        dto.setRejectionReason(pendingUser.getRejectionReason());
        dto.setRequestedAt(pendingUser.getRequestedAt());
        dto.setReviewedAt(pendingUser.getReviewedAt());

        List<DocumentDTO> documentDTOs = pendingUser.getDocuments().stream().map(this::convertDocumentToDTO)
                .collect(Collectors.toList());
        dto.setDocuments(documentDTOs);
        dto.setDocumentCount(documentDTOs.size());

        return dto;
    }

    private DocumentDTO convertDocumentToDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setDocumentType(document.getDocumentType());
        dto.setFileName(document.getFileName());
        dto.setFilePath(document.getFilePath());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setUploadedBy(document.getUploadedBy());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setVerified(document.isVerified());
        dto.setVerifiedBy(document.getVerifiedBy());
        dto.setVerifiedAt(document.getVerifiedAt());
        return dto;
    }
}
