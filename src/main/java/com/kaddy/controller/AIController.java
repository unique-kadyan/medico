package com.kaddy.controller;

import com.kaddy.dto.ai.*;
import com.kaddy.service.AIService;
import com.kaddy.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final HospitalService hospitalService;

    @PostMapping("/symptoms/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<SymptomAnalysisResponse> analyzeSymptoms(@RequestHeader("X-Hospital-Id") Long hospitalId,
            @Valid @RequestBody SymptomAnalysisRequest request) {
        hospitalService.validateFeatureAccess(hospitalId, "ai");
        return ResponseEntity.ok(aiService.analyzeSymptoms(request));
    }

    @PostMapping("/drugs/interactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<DrugInteractionResponse> checkDrugInteractions(
            @RequestHeader("X-Hospital-Id") Long hospitalId, @Valid @RequestBody DrugInteractionRequest request) {
        hospitalService.validateFeatureAccess(hospitalId, "ai");
        return ResponseEntity.ok(aiService.checkDrugInteractions(request));
    }

    @PostMapping("/scheduling/suggest")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<SmartSchedulingResponse> suggestAppointmentSlots(
            @RequestHeader("X-Hospital-Id") Long hospitalId, @Valid @RequestBody SmartSchedulingRequest request) {
        hospitalService.validateFeatureAccess(hospitalId, "ai");
        return ResponseEntity.ok(aiService.suggestAppointmentSlots(request));
    }

    @PostMapping("/summary/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<MedicalSummaryResponse> generateMedicalSummary(
            @RequestHeader("X-Hospital-Id") Long hospitalId, @Valid @RequestBody MedicalSummaryRequest request) {
        hospitalService.validateFeatureAccess(hospitalId, "ai");
        return ResponseEntity.ok(aiService.generateMedicalSummary(request));
    }
}
