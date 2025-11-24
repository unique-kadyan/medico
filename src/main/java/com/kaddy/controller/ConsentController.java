package com.kaddy.controller;

import com.kaddy.dto.consent.ConsentRequestDTO;
import com.kaddy.dto.consent.PatientConsentDTO;
import com.kaddy.model.User;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.ConsentService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/consent")
@RequiredArgsConstructor
@Slf4j
public class ConsentController {

    private final ConsentService consentService;
    private final SecurityUtils securityUtils;

    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<PatientConsentDTO> createConsentRequest(@Valid @RequestBody ConsentRequestDTO request) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        log.info("Creating consent request for patient {} by user {}", request.getPatientId(), currentUser.getId());

        PatientConsentDTO consent = consentService.createConsentRequest(request,
                currentUser.getHospital() != null ? currentUser.getHospital().getId() : null, currentUser.getId());

        return new ResponseEntity<>(consent, HttpStatus.CREATED);
    }

    @PostMapping("/{consentId}/grant")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<PatientConsentDTO> grantConsent(@PathVariable Long consentId,
            @RequestParam(required = false, defaultValue = "IN_PERSON_VERIFICATION") String verificationMethod) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        log.info("Granting consent {} by user {}", consentId, currentUser.getId());

        PatientConsentDTO consent = consentService.grantConsent(consentId, currentUser.getId(), verificationMethod);

        return ResponseEntity.ok(consent);
    }

    @PostMapping("/{consentId}/deny")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<PatientConsentDTO> denyConsent(@PathVariable Long consentId,
            @RequestParam(required = false) String reason) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        log.info("Denying consent {} by user {}", consentId, currentUser.getId());

        PatientConsentDTO consent = consentService.denyConsent(consentId, currentUser.getId(),
                reason != null ? reason : "Patient declined");

        return ResponseEntity.ok(consent);
    }

    @PostMapping("/{consentId}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<PatientConsentDTO> revokeConsent(@PathVariable Long consentId,
            @RequestParam(required = false) String reason) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        log.info("Revoking consent {} by user {}", consentId, currentUser.getId());

        PatientConsentDTO consent = consentService.revokeConsent(consentId, currentUser.getId(),
                reason != null ? reason : "Patient requested revocation");

        return ResponseEntity.ok(consent);
    }

    @GetMapping("/{consentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<PatientConsentDTO> getConsent(@PathVariable Long consentId) {
        log.info("Getting consent by ID: {}", consentId);
        return ResponseEntity.ok(consentService.getConsentById(consentId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<List<PatientConsentDTO>> getConsentsByPatient(@PathVariable Long patientId) {
        log.info("Getting consents for patient: {}", patientId);
        return ResponseEntity.ok(consentService.getConsentsByPatient(patientId));
    }

    @GetMapping("/hospital/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Page<PatientConsentDTO>> getPendingConsents(@PageableDefault(size = 20) Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital() != null ? currentUser.getHospital().getId() : null;
        if (hospitalId == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }

        log.info("Getting pending consents for hospital: {}", hospitalId);
        return ResponseEntity.ok(consentService.getPendingConsents(hospitalId, pageable));
    }

    @GetMapping("/hospital/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Page<PatientConsentDTO>> getAllHospitalConsents(
            @PageableDefault(size = 20) Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Long hospitalId = currentUser.getHospital() != null ? currentUser.getHospital().getId() : null;
        if (hospitalId == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }

        log.info("Getting all consents for hospital: {}", hospitalId);
        return ResponseEntity.ok(consentService.getConsentsByHospital(hospitalId, pageable));
    }

    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> checkValidConsent(@RequestParam Long patientId,
            @RequestParam Long sourceHospitalId, @RequestParam Long targetHospitalId) {
        log.info("Checking consent for patient {} from {} to {}", patientId, sourceHospitalId, targetHospitalId);

        boolean hasConsent = consentService.hasValidConsent(patientId, sourceHospitalId, targetHospitalId);

        return ResponseEntity.ok(Map.of("patientId", patientId, "sourceHospitalId", sourceHospitalId,
                "targetHospitalId", targetHospitalId, "hasValidConsent", hasConsent));
    }
}
