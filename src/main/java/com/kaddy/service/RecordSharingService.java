package com.kaddy.service;

import com.kaddy.dto.consent.*;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.*;
import com.kaddy.model.enums.AuditActionType;
import com.kaddy.model.enums.ConsentStatus;
import com.kaddy.model.enums.SharingScope;
import com.kaddy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordSharingService {

    private final RecordShareRequestRepository shareRequestRepository;
    private final PatientConsentRepository consentRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final LabTestRepository labTestRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AccessAuditLogRepository auditLogRepository;
    private final FHIRService fhirService;
    private final ConsentService consentService;

    @Transactional
    public RecordShareRequestDTO createShareRequest(CreateShareRequestDTO request, Long requestingHospitalId) {
        Hospital requestingHospital = hospitalRepository.findById(requestingHospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Requesting hospital not found"));

        Hospital sourceHospital = hospitalRepository.findById(request.getSourceHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Source hospital not found"));

        Doctor requestingDoctor = doctorRepository.findById(request.getRequestingDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Requesting doctor not found"));

        Patient patient = findOrIdentifyPatient(request, sourceHospital);

        if (shareRequestRepository.existsPendingRequest(patient.getId(), requestingHospitalId,
                request.getSourceHospitalId())) {
            throw new IllegalStateException("A pending request already exists for this patient");
        }

        RecordShareRequest shareRequest = new RecordShareRequest();
        shareRequest.setRequestNumber(generateRequestNumber());
        shareRequest.setPatient(patient);
        shareRequest.setRequestingHospital(requestingHospital);
        shareRequest.setSourceHospital(sourceHospital);
        shareRequest.setRequestingDoctor(requestingDoctor);
        shareRequest.setStatus(ConsentStatus.PENDING);
        shareRequest.setRequestedScope(request.getRequestedScope());
        shareRequest.setClinicalPurpose(request.getClinicalPurpose());
        shareRequest.setUrgencyLevel(request.getUrgencyLevel());
        shareRequest.setPatientIdentifier(request.getPatientIdentifier());
        shareRequest.setPatientDateOfBirth(request.getPatientDateOfBirth());
        shareRequest.setPatientPhone(request.getPatientPhone());
        shareRequest.setPatientEmail(request.getPatientEmail());
        shareRequest.setValidUntil(request.getValidUntil());
        shareRequest.setAdditionalNotes(request.getAdditionalNotes());
        shareRequest.setExpiresAt(LocalDateTime.now().plusDays(30)); // Request expires in 30 days

        RecordShareRequest savedRequest = shareRequestRepository.save(shareRequest);

        createAuditLog(AuditActionType.SHARE_REQUEST_CREATED, null, patient, requestingHospital, sourceHospital, null,
                savedRequest, "Record share request created by " + requestingHospital.getName());

        log.info("Share request created: {} from {} to {}", savedRequest.getRequestNumber(),
                requestingHospital.getName(), sourceHospital.getName());

        return mapToDTO(savedRequest);
    }

    @Transactional
    public RecordShareRequestDTO approveShareRequest(Long requestId, Long approvedByUserId, String responseNotes) {
        RecordShareRequest request = shareRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found"));

        if (request.getStatus() != ConsentStatus.PENDING) {
            throw new IllegalStateException("Request is not in pending state");
        }

        User approvedBy = userRepository.findById(approvedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean hasConsent = consentService.hasValidConsent(request.getPatient().getId(),
                request.getSourceHospital().getId(), request.getRequestingHospital().getId());

        if (!hasConsent) {
            throw new IllegalStateException("Patient consent is required before approving share request");
        }

        request.setStatus(ConsentStatus.APPROVED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRespondedBy(approvedBy);
        request.setResponseNotes(responseNotes);

        RecordShareRequest savedRequest = shareRequestRepository.save(request);

        createAuditLog(AuditActionType.SHARE_REQUEST_APPROVED, approvedBy, request.getPatient(),
                request.getSourceHospital(), request.getRequestingHospital(), null, savedRequest,
                "Share request approved");

        log.info("Share request approved: {}", requestId);

        return mapToDTO(savedRequest);
    }

    @Transactional
    public RecordShareRequestDTO denyShareRequest(Long requestId, Long deniedByUserId, String reason) {
        RecordShareRequest request = shareRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found"));

        if (request.getStatus() != ConsentStatus.PENDING) {
            throw new IllegalStateException("Request is not in pending state");
        }

        User deniedBy = userRepository.findById(deniedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        request.setStatus(ConsentStatus.DENIED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRespondedBy(deniedBy);
        request.setResponseNotes(reason);

        RecordShareRequest savedRequest = shareRequestRepository.save(request);

        createAuditLog(AuditActionType.SHARE_REQUEST_DENIED, deniedBy, request.getPatient(),
                request.getSourceHospital(), request.getRequestingHospital(), null, savedRequest,
                "Share request denied. Reason: " + reason);

        log.info("Share request denied: {} reason: {}", requestId, reason);

        return mapToDTO(savedRequest);
    }

    @Transactional
    public SharedMedicalRecordDTO getSharedRecords(Long requestId, Long accessedByUserId) {
        RecordShareRequest request = shareRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found"));

        if (!request.isAccessValid()) {
            throw new IllegalStateException("Share request is not valid for access");
        }

        boolean hasConsent = consentService.hasValidConsent(request.getPatient().getId(),
                request.getSourceHospital().getId(), request.getRequestingHospital().getId());

        if (!hasConsent) {
            throw new IllegalStateException("Patient consent is no longer valid");
        }

        Patient patient = request.getPatient();
        SharingScope scope = request.getRequestedScope();

        SharedMedicalRecordDTO sharedRecords = new SharedMedicalRecordDTO();
        sharedRecords.setShareRequestNumber(request.getRequestNumber());
        sharedRecords.setSharingScope(scope.name());
        sharedRecords.setSharedAt(LocalDateTime.now());
        sharedRecords.setValidUntil(request.getValidUntil());
        sharedRecords.setSourceHospitalName(request.getSourceHospital().getName());
        sharedRecords.setSourceHospitalCode(request.getSourceHospital().getCode());

        sharedRecords.setPatient(buildPatientSummary(patient));

        populateRecordsBasedOnScope(sharedRecords, patient, scope);

        if (request.getSourceHospital().getFhirEnabled()) {
            try {
                String fhirBundle = fhirService.getPatientBundleJson(patient.getId());
                sharedRecords.setFhirBundle(fhirBundle);
            } catch (Exception e) {
                log.warn("Failed to generate FHIR bundle for patient {}: {}", patient.getId(), e.getMessage());
            }
        }

        request.setDataSharedAt(LocalDateTime.now());
        request.setShareMethod("API");
        shareRequestRepository.save(request);

        PatientConsent consent = consentRepository.findActiveConsent(patient.getId(),
                request.getSourceHospital().getId(), request.getRequestingHospital().getId(), ConsentStatus.APPROVED)
                .orElse(null);

        if (consent != null) {
            consentService.recordAccess(consent.getId(), accessedByUserId);
        }

        User accessedBy = userRepository.findById(accessedByUserId).orElse(null);
        createAuditLog(AuditActionType.RECORD_SHARED, accessedBy, patient, request.getSourceHospital(),
                request.getRequestingHospital(), consent, request,
                "Medical records shared via request " + request.getRequestNumber());

        log.info("Records shared for request: {} to hospital: {}", request.getRequestNumber(),
                request.getRequestingHospital().getName());

        return sharedRecords;
    }

    @Transactional(readOnly = true)
    public Page<RecordShareRequestDTO> getIncomingRequests(Long hospitalId, Pageable pageable) {
        return shareRequestRepository.findBySourceHospitalId(hospitalId, pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecordShareRequestDTO> getOutgoingRequests(Long hospitalId, Pageable pageable) {
        return shareRequestRepository.findByRequestingHospitalId(hospitalId, pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<RecordShareRequestDTO> getPendingRequests(Long hospitalId) {
        return shareRequestRepository.findPendingRequestsForHospital(hospitalId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecordShareRequestDTO getShareRequest(Long requestId) {
        RecordShareRequest request = shareRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found"));
        return mapToDTO(request);
    }

    @Transactional(readOnly = true)
    public RecordShareRequestDTO getShareRequestByNumber(String requestNumber) {
        RecordShareRequest request = shareRequestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Share request not found"));
        return mapToDTO(request);
    }

    private Patient findOrIdentifyPatient(CreateShareRequestDTO request, Hospital sourceHospital) {
        return patientRepository.findByPhone(request.getPatientPhone())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with the provided identifiers. "
                        + "Patient must be registered at the source hospital."));
    }

    private String generateRequestNumber() {
        String prefix = "RSR-" + Year.now().getValue() + "-";
        String maxNumber = shareRequestRepository.findMaxRequestNumber(prefix);
        int nextNumber = 1;
        if (maxNumber != null) {
            String numPart = maxNumber.replace(prefix, "");
            nextNumber = Integer.parseInt(numPart) + 1;
        }
        return prefix + String.format("%05d", nextNumber);
    }

    private SharedMedicalRecordDTO.PatientSummary buildPatientSummary(Patient patient) {
        return SharedMedicalRecordDTO.PatientSummary.builder().patientId(patient.getId().toString())
                .firstName(patient.getFirstName()).lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null)
                .gender(patient.getGender() != null ? patient.getGender().toString() : null)
                .bloodGroup(patient.getBloodGroup() != null ? patient.getBloodGroup().toString() : null)
                .phone(patient.getPhone()).email(patient.getEmail()).address(patient.getAddress())
                .emergencyContact(patient.getEmergencyContact()).build();
    }

    private void populateRecordsBasedOnScope(SharedMedicalRecordDTO dto, Patient patient, SharingScope scope) {
        switch (scope) {
            case ALL_RECORDS :
                populateAllRecords(dto, patient);
                break;
            case MEDICAL_HISTORY :
                dto.setMedicalHistory(getMedicalHistory(patient.getId()));
                break;
            case LAB_RESULTS :
                dto.setLabResults(getLabResults(patient.getId()));
                break;
            case PRESCRIPTIONS :
                dto.setPrescriptions(getPrescriptions(patient.getId()));
                break;
            case DIAGNOSES :
                dto.setDiagnoses(getDiagnoses(patient.getId()));
                break;
            case VITAL_SIGNS :
                dto.setVitalSigns(getVitalSigns(patient.getId()));
                break;
            case SUMMARY_ONLY :
                dto.setClinicalSummary(generateClinicalSummary(patient));
                break;
            default :
                dto.setClinicalSummary(generateClinicalSummary(patient));
        }
    }

    private void populateAllRecords(SharedMedicalRecordDTO dto, Patient patient) {
        dto.setMedicalHistory(getMedicalHistory(patient.getId()));
        dto.setLabResults(getLabResults(patient.getId()));
        dto.setPrescriptions(getPrescriptions(patient.getId()));
        dto.setDiagnoses(getDiagnoses(patient.getId()));
        dto.setVitalSigns(getVitalSigns(patient.getId()));
        dto.setClinicalSummary(generateClinicalSummary(patient));
    }

    private List<SharedMedicalRecordDTO.MedicalHistoryItem> getMedicalHistory(Long patientId) {
        return medicalRecordRepository.findPatientRecordsOrderedByDate(patientId).stream()
                .map(record -> SharedMedicalRecordDTO.MedicalHistoryItem.builder().id(record.getId())
                        .recordDate(record.getRecordDate()).chiefComplaint(record.getChiefComplaint())
                        .diagnosis(record.getDiagnosis()).treatment(record.getTreatment())
                        .doctorName(record.getDoctor() != null
                                ? "Dr. " + record.getDoctor().getUser().getFirstName() + " "
                                        + record.getDoctor().getUser().getLastName()
                                : null)
                        .notes(record.getNotes()).build())
                .collect(Collectors.toList());
    }

    private List<SharedMedicalRecordDTO.LabResultItem> getLabResults(Long patientId) {
        return labTestRepository.findByPatientId(patientId).stream()
                .map(test -> SharedMedicalRecordDTO.LabResultItem.builder().id(test.getId())
                        .testName(test.getTestName()).testCode(test.getTestType()).testDate(test.getResultDate())
                        .result(test.getTestResults()).normalRange(test.getNormalRange())
                        .status(test.getStatus() != null ? test.getStatus().toString() : null).notes(test.getRemarks())
                        .build())
                .collect(Collectors.toList());
    }

    private List<SharedMedicalRecordDTO.PrescriptionItem> getPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(rx -> SharedMedicalRecordDTO.PrescriptionItem.builder().id(rx.getId())
                        .prescribedDate(
                                rx.getPrescriptionDate() != null ? rx.getPrescriptionDate().atStartOfDay() : null)
                        .medicationName(rx.getDiagnosis()).dosage(rx.getInstructions()).frequency(null).duration(null)
                        .prescribedBy(rx.getDoctor() != null
                                ? "Dr. " + rx.getDoctor().getUser().getFirstName() + " "
                                        + rx.getDoctor().getUser().getLastName()
                                : null)
                        .notes(rx.getInstructions()).build())
                .collect(Collectors.toList());
    }

    private List<SharedMedicalRecordDTO.DiagnosisItem> getDiagnoses(Long patientId) {
        return medicalRecordRepository.findPatientRecordsOrderedByDate(patientId).stream()
                .filter(record -> record.getDiagnosis() != null && !record.getDiagnosis().isEmpty())
                .map(record -> SharedMedicalRecordDTO.DiagnosisItem.builder().id(record.getId())
                        .diagnosisDate(record.getRecordDate()).diagnosisName(record.getDiagnosis())
                        .diagnosedBy(record.getDoctor() != null
                                ? "Dr. " + record.getDoctor().getUser().getFirstName() + " "
                                        + record.getDoctor().getUser().getLastName()
                                : null)
                        .build())
                .collect(Collectors.toList());
    }

    private List<SharedMedicalRecordDTO.VitalSignItem> getVitalSigns(Long patientId) {
        return medicalRecordRepository.findPatientRecordsOrderedByDate(patientId).stream()
                .filter(record -> record.getBloodPressure() != null || record.getHeartRate() != null
                        || record.getTemperature() != null)
                .map(record -> SharedMedicalRecordDTO.VitalSignItem.builder().id(record.getId())
                        .recordedAt(record.getRecordDate()).bloodPressure(record.getBloodPressure())
                        .heartRate(record.getHeartRate() != null ? String.valueOf(record.getHeartRate()) : null)
                        .temperature(record.getTemperature() != null ? String.valueOf(record.getTemperature()) : null)
                        .respiratoryRate(record.getRespiratoryRate() != null
                                ? String.valueOf(record.getRespiratoryRate())
                                : null)
                        .weight(record.getWeight() != null ? String.valueOf(record.getWeight()) : null)
                        .height(record.getHeight() != null ? String.valueOf(record.getHeight()) : null).build())
                .collect(Collectors.toList());
    }

    private String generateClinicalSummary(Patient patient) {
        StringBuilder summary = new StringBuilder();
        summary.append("Patient: ").append(patient.getFirstName()).append(" ").append(patient.getLastName())
                .append("\n");
        if (patient.getDateOfBirth() != null) {
            summary.append("DOB: ").append(patient.getDateOfBirth()).append("\n");
        }
        if (patient.getBloodGroup() != null) {
            summary.append("Blood Group: ").append(patient.getBloodGroup()).append("\n");
        }

        long diagnosisCount = medicalRecordRepository.findPatientRecordsOrderedByDate(patient.getId()).stream()
                .filter(r -> r.getDiagnosis() != null).count();
        summary.append("Total diagnoses on record: ").append(diagnosisCount).append("\n");

        long labTestCount = labTestRepository.findByPatientId(patient.getId()).size();
        summary.append("Total lab tests: ").append(labTestCount).append("\n");

        return summary.toString();
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

    private RecordShareRequestDTO mapToDTO(RecordShareRequest request) {
        return RecordShareRequestDTO.builder().id(request.getId()).requestNumber(request.getRequestNumber())
                .patientId(request.getPatient().getId())
                .patientName(request.getPatient().getFirstName() + " " + request.getPatient().getLastName())
                .requestingHospitalId(request.getRequestingHospital().getId())
                .requestingHospitalName(request.getRequestingHospital().getName())
                .sourceHospitalId(request.getSourceHospital().getId())
                .sourceHospitalName(request.getSourceHospital().getName())
                .requestingDoctorId(request.getRequestingDoctor().getId())
                .requestingDoctorName("Dr. " + request.getRequestingDoctor().getUser().getFirstName() + " "
                        + request.getRequestingDoctor().getUser().getLastName())
                .status(request.getStatus()).requestedScope(request.getRequestedScope())
                .clinicalPurpose(request.getClinicalPurpose()).urgencyLevel(request.getUrgencyLevel())
                .patientIdentifier(request.getPatientIdentifier()).patientDateOfBirth(request.getPatientDateOfBirth())
                .patientPhone(request.getPatientPhone()).patientEmail(request.getPatientEmail())
                .consentId(request.getConsent() != null ? request.getConsent().getId() : null)
                .respondedAt(request.getRespondedAt())
                .respondedById(request.getRespondedBy() != null ? request.getRespondedBy().getId() : null)
                .respondedByName(request.getRespondedBy() != null
                        ? request.getRespondedBy().getFirstName() + " " + request.getRespondedBy().getLastName()
                        : null)
                .responseNotes(request.getResponseNotes()).dataSharedAt(request.getDataSharedAt())
                .shareMethod(request.getShareMethod()).sharedRecordIds(request.getSharedRecordIds())
                .expiresAt(request.getExpiresAt()).validUntil(request.getValidUntil())
                .additionalNotes(request.getAdditionalNotes()).createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt()).isPending(request.isPending()).isApproved(request.isApproved())
                .isExpired(request.isExpired()).isAccessValid(request.isAccessValid()).build();
    }
}
