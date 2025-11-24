package com.kaddy.service;

import com.kaddy.dto.consent.ConsentRequestDTO;
import com.kaddy.dto.consent.PatientConsentDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.*;
import com.kaddy.model.enums.AuditActionType;
import com.kaddy.model.enums.ConsentStatus;
import com.kaddy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentService {

    private final PatientConsentRepository consentRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final AccessAuditLogRepository auditLogRepository;

    @Transactional
    public PatientConsentDTO createConsentRequest(ConsentRequestDTO request, Long sourceHospitalId,
            Long requestedByUserId) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Hospital sourceHospital = hospitalRepository.findById(sourceHospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Source hospital not found"));

        Hospital targetHospital = hospitalRepository.findById(request.getTargetHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Target hospital not found"));

        User requestedBy = userRepository.findById(requestedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (consentRepository.hasValidConsent(request.getPatientId(), sourceHospitalId, request.getTargetHospitalId(),
                LocalDateTime.now())) {
            throw new IllegalStateException("Valid consent already exists for this patient and hospital combination");
        }

        PatientConsent consent = new PatientConsent();
        consent.setPatient(patient);
        consent.setSourceHospital(sourceHospital);
        consent.setTargetHospital(targetHospital);
        consent.setStatus(ConsentStatus.PENDING);
        consent.setSharingScope(request.getSharingScope());
        consent.setPurpose(request.getPurpose());
        consent.setExpiresAt(request.getExpiresAt());
        consent.setRequestedBy(requestedBy);
        consent.setNotes(request.getNotes());

        if (request.getSpecificRecordIds() != null && !request.getSpecificRecordIds().isEmpty()) {
            consent.setSpecificRecordIds(
                    request.getSpecificRecordIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        }

        PatientConsent savedConsent = consentRepository.save(consent);

        createAuditLog(AuditActionType.CONSENT_REQUESTED, requestedBy, patient, sourceHospital, targetHospital,
                savedConsent, null, "Consent requested for sharing records with " + targetHospital.getName());

        log.info("Consent request created: {} for patient {} to hospital {}", savedConsent.getId(), patient.getId(),
                targetHospital.getName());

        return mapToDTO(savedConsent);
    }

    @Transactional
    public PatientConsentDTO grantConsent(Long consentId, Long approvedByUserId, String verificationMethod) {
        PatientConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found"));

        if (consent.getStatus() != ConsentStatus.PENDING) {
            throw new IllegalStateException("Consent is not in pending state");
        }

        User approvedBy = userRepository.findById(approvedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        consent.setStatus(ConsentStatus.APPROVED);
        consent.setConsentGrantedAt(LocalDateTime.now());
        consent.setApprovedBy(approvedBy);
        consent.setPatientVerificationMethod(verificationMethod);
        consent.setPatientVerifiedAt(LocalDateTime.now());

        PatientConsent savedConsent = consentRepository.save(consent);

        createAuditLog(AuditActionType.CONSENT_GRANTED, approvedBy, consent.getPatient(), consent.getSourceHospital(),
                consent.getTargetHospital(), savedConsent, null, "Patient granted consent for record sharing");

        log.info("Consent granted: {} by user {}", consentId, approvedByUserId);

        return mapToDTO(savedConsent);
    }

    @Transactional
    public PatientConsentDTO denyConsent(Long consentId, Long deniedByUserId, String reason) {
        PatientConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found"));

        if (consent.getStatus() != ConsentStatus.PENDING) {
            throw new IllegalStateException("Consent is not in pending state");
        }

        User deniedBy = userRepository.findById(deniedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        consent.setStatus(ConsentStatus.DENIED);
        consent.setRevokeReason(reason);
        consent.setApprovedBy(deniedBy);

        PatientConsent savedConsent = consentRepository.save(consent);

        createAuditLog(AuditActionType.CONSENT_DENIED, deniedBy, consent.getPatient(), consent.getSourceHospital(),
                consent.getTargetHospital(), savedConsent, null, "Patient denied consent. Reason: " + reason);

        log.info("Consent denied: {} reason: {}", consentId, reason);

        return mapToDTO(savedConsent);
    }

    @Transactional
    public PatientConsentDTO revokeConsent(Long consentId, Long revokedByUserId, String reason) {
        PatientConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found"));

        if (consent.getStatus() != ConsentStatus.APPROVED) {
            throw new IllegalStateException("Only approved consents can be revoked");
        }

        User revokedBy = userRepository.findById(revokedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        consent.setStatus(ConsentStatus.REVOKED);
        consent.setConsentRevokedAt(LocalDateTime.now());
        consent.setRevokeReason(reason);

        PatientConsent savedConsent = consentRepository.save(consent);

        createAuditLog(AuditActionType.CONSENT_REVOKED, revokedBy, consent.getPatient(), consent.getSourceHospital(),
                consent.getTargetHospital(), savedConsent, null, "Patient revoked consent. Reason: " + reason);

        log.info("Consent revoked: {} reason: {}", consentId, reason);

        return mapToDTO(savedConsent);
    }

    @Transactional(readOnly = true)
    public boolean hasValidConsent(Long patientId, Long sourceHospitalId, Long targetHospitalId) {
        return consentRepository.hasValidConsent(patientId, sourceHospitalId, targetHospitalId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public PatientConsentDTO getConsentById(Long consentId) {
        PatientConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found"));
        return mapToDTO(consent);
    }

    @Transactional(readOnly = true)
    public List<PatientConsentDTO> getConsentsByPatient(Long patientId) {
        return consentRepository.findByPatientId(patientId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PatientConsentDTO> getPendingConsents(Long hospitalId, Pageable pageable) {
        return consentRepository.findBySourceHospitalIdAndStatus(hospitalId, ConsentStatus.PENDING, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PatientConsentDTO> getConsentsByHospital(Long hospitalId, Pageable pageable) {
        return consentRepository.findBySourceHospitalId(hospitalId, pageable).map(this::mapToDTO);
    }

    @Transactional
    public void recordAccess(Long consentId, Long accessedByUserId) {
        PatientConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found"));

        if (!consent.isValid()) {
            throw new IllegalStateException("Consent is not valid for access");
        }

        consent.incrementAccessCount();
        consentRepository.save(consent);

        User accessedBy = userRepository.findById(accessedByUserId).orElse(null);

        createAuditLog(AuditActionType.RECORD_ACCESSED, accessedBy, consent.getPatient(), consent.getSourceHospital(),
                consent.getTargetHospital(), consent, null, "Shared records accessed via consent");
    }

    @Transactional
    public void expireOldConsents() {
        List<PatientConsent> expiredConsents = consentRepository.findExpiredConsents(LocalDateTime.now());
        for (PatientConsent consent : expiredConsents) {
            consent.setStatus(ConsentStatus.EXPIRED);
            consentRepository.save(consent);

            createAuditLog(AuditActionType.CONSENT_EXPIRED, null, consent.getPatient(), consent.getSourceHospital(),
                    consent.getTargetHospital(), consent, null, "Consent expired automatically");

            log.info("Consent expired: {}", consent.getId());
        }
    }

    private void createAuditLog(AuditActionType actionType, User performedBy, Patient patient, Hospital hospital,
            Hospital targetHospital, PatientConsent consent, RecordShareRequest shareRequest, String description) {
        AccessAuditLog auditLog = AccessAuditLog.create(actionType, performedBy, patient);
        auditLog.setHospital(hospital);
        auditLog.setTargetHospital(targetHospital);
        auditLog.setConsent(consent);
        auditLog.setShareRequest(shareRequest);
        auditLog.setActionDescription(description);
        auditLog.setDataClassification("PHI");
        auditLogRepository.save(auditLog);
    }

    private PatientConsentDTO mapToDTO(PatientConsent consent) {
        return PatientConsentDTO.builder().id(consent.getId()).patientId(consent.getPatient().getId())
                .patientName(consent.getPatient().getFirstName() + " " + consent.getPatient().getLastName())
                .sourceHospitalId(consent.getSourceHospital().getId())
                .sourceHospitalName(consent.getSourceHospital().getName())
                .targetHospitalId(consent.getTargetHospital().getId())
                .targetHospitalName(consent.getTargetHospital().getName()).status(consent.getStatus())
                .sharingScope(consent.getSharingScope()).purpose(consent.getPurpose())
                .consentGrantedAt(consent.getConsentGrantedAt()).consentRevokedAt(consent.getConsentRevokedAt())
                .expiresAt(consent.getExpiresAt()).revokeReason(consent.getRevokeReason())
                .patientVerificationMethod(consent.getPatientVerificationMethod())
                .patientVerifiedAt(consent.getPatientVerifiedAt()).specificRecordIds(consent.getSpecificRecordIds())
                .requestedById(consent.getRequestedBy() != null ? consent.getRequestedBy().getId() : null)
                .requestedByName(consent.getRequestedBy() != null
                        ? consent.getRequestedBy().getFirstName() + " " + consent.getRequestedBy().getLastName()
                        : null)
                .approvedById(consent.getApprovedBy() != null ? consent.getApprovedBy().getId() : null)
                .approvedByName(consent.getApprovedBy() != null
                        ? consent.getApprovedBy().getFirstName() + " " + consent.getApprovedBy().getLastName()
                        : null)
                .notes(consent.getNotes()).accessCount(consent.getAccessCount())
                .lastAccessedAt(consent.getLastAccessedAt()).createdAt(consent.getCreatedAt())
                .updatedAt(consent.getUpdatedAt()).isValid(consent.isValid()).isExpired(consent.isExpired()).build();
    }
}
