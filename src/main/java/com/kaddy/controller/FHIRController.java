package com.kaddy.controller;

import com.kaddy.dto.fhir.FHIRMedicationRequestDTO;
import com.kaddy.dto.fhir.FHIRObservationDTO;
import com.kaddy.dto.fhir.FHIRPatientDTO;
import com.kaddy.service.FHIRService;
import com.kaddy.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fhir")
@RequiredArgsConstructor
public class FHIRController {

    private final FHIRService fhirService;
    private final HospitalService hospitalService;

    @GetMapping(value = "/Patient/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<String> getFHIRPatientJson(@RequestHeader("X-Hospital-Id") Long hospitalId,
            @PathVariable Long patientId) {
        hospitalService.validateFeatureAccess(hospitalId, "fhir");
        return ResponseEntity.ok(fhirService.getFHIRPatientJson(patientId));
    }

    @GetMapping(value = "/Patient/{patientId}", produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<String> getFHIRPatientXml(@RequestHeader("X-Hospital-Id") Long hospitalId,
            @PathVariable Long patientId) {
        hospitalService.validateFeatureAccess(hospitalId, "fhir");
        return ResponseEntity.ok(fhirService.getFHIRPatientXml(patientId));
    }

    @GetMapping(value = "/Practitioner/{doctorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<String> getFHIRPractitioner(@RequestHeader("X-Hospital-Id") Long hospitalId,
            @PathVariable Long doctorId) {
        hospitalService.validateFeatureAccess(hospitalId, "fhir");
        return ResponseEntity.ok(fhirService.getFHIRPractitionerJson(doctorId));
    }

    @PostMapping(value = "/Observation", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<String> createVitalSignsObservation(@RequestHeader("X-Hospital-Id") Long hospitalId,
            @RequestBody FHIRObservationDTO observationDTO) {
        hospitalService.validateFeatureAccess(hospitalId, "fhir");
        return ResponseEntity.ok(fhirService.getVitalSignsObservationJson(observationDTO.getPatientId(),
                observationDTO.getType(), observationDTO.getValue(), observationDTO.getUnit()));
    }

    @PostMapping(value = "/MedicationRequest", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<String> createMedicationRequest(@RequestHeader("X-Hospital-Id") Long hospitalId,
            @RequestBody FHIRMedicationRequestDTO requestDTO) {
        hospitalService.validateFeatureAccess(hospitalId, "fhir");
        return ResponseEntity
                .ok(fhirService.getMedicationRequestJson(requestDTO.getPatientId(), requestDTO.getMedicationId(),
                        requestDTO.getDoctorId(), requestDTO.getDosage(), requestDTO.getFrequency()));
    }

    @GetMapping(value = "/Bundle/Patient/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<String> getPatientBundle(@RequestHeader("X-Hospital-Id") Long hospitalId,
            @PathVariable Long patientId) {
        hospitalService.validateFeatureAccess(hospitalId, "fhir");
        return ResponseEntity.ok(fhirService.getPatientBundleJson(patientId));
    }

    @PostMapping(value = "/Patient/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<FHIRPatientDTO> importFHIRPatient(@RequestHeader("X-Hospital-Id") Long hospitalId,
            @RequestBody String fhirPatientJson) {
        hospitalService.validateFeatureAccess(hospitalId, "fhir");
        return ResponseEntity.ok(fhirService.parseFHIRPatient(fhirPatientJson));
    }
}
