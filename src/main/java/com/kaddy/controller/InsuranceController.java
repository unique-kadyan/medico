package com.kaddy.controller;

import com.kaddy.model.InsuranceClaim;
import com.kaddy.model.InsuranceProvider;
import com.kaddy.model.PatientInsurance;
import com.kaddy.model.User;
import com.kaddy.model.enums.InsuranceClaimStatus;
import com.kaddy.security.SecurityUtils;
import com.kaddy.service.InsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insurance")
@RequiredArgsConstructor
@Slf4j
public class InsuranceController {

    private final InsuranceService insuranceService;
    private final SecurityUtils securityUtils;

    @PostMapping("/providers")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<InsuranceProvider> createProvider(@RequestParam String providerCode,
            @RequestParam String name, @RequestParam(required = false) String address,
            @RequestParam(required = false) String phone, @RequestParam(required = false) String email,
            @RequestParam(required = false) String contactPerson, @RequestParam(required = false) String tpaName,
            @RequestParam(required = false) String tpaCode,
            @RequestParam(required = false) BigDecimal defaultCoveragePercentage,
            @RequestParam(required = false) Integer claimProcessingDays) {

        InsuranceProvider provider = insuranceService.createProvider(providerCode, name, address, phone, email,
                contactPerson, tpaName, tpaCode, defaultCoveragePercentage, claimProcessingDays);
        return new ResponseEntity<>(provider, HttpStatus.CREATED);
    }

    @PutMapping("/providers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<InsuranceProvider> updateProvider(@PathVariable Long id,
            @RequestParam(required = false) String name, @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email, @RequestParam(required = false) String contactPerson,
            @RequestParam(required = false) BigDecimal defaultCoveragePercentage,
            @RequestParam(required = false) Boolean isActive) {

        InsuranceProvider provider = insuranceService.updateProvider(id, name, phone, email, contactPerson,
                defaultCoveragePercentage, isActive);
        return ResponseEntity.ok(provider);
    }

    @GetMapping("/providers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<InsuranceProvider> getProvider(@PathVariable Long id) {
        return ResponseEntity.ok(insuranceService.getProvider(id));
    }

    @GetMapping("/providers")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<?> getProviders(@RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(insuranceService.searchProviders(search, pageable));
        } else {
            return ResponseEntity.ok(insuranceService.getActiveProviders());
        }
    }

    @PostMapping("/patients/{patientId}/insurance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientInsurance> addPatientInsurance(@PathVariable Long patientId,
            @RequestParam Long providerId, @RequestParam String policyNumber,
            @RequestParam(required = false) String groupNumber, @RequestParam(required = false) String memberId,
            @RequestParam(required = false) String planName, @RequestParam(required = false) String planType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate,
            @RequestParam(required = false) BigDecimal annualLimit,
            @RequestParam(required = false) BigDecimal deductible,
            @RequestParam(required = false) BigDecimal coPayPercentage,
            @RequestParam(required = false) Boolean isPrimary, @RequestParam(required = false) String policyHolderName,
            @RequestParam(required = false) String policyHolderRelation,
            @RequestParam(required = false) Boolean requiresPreAuth) {

        PatientInsurance insurance = insuranceService.addPatientInsurance(patientId, providerId, policyNumber,
                groupNumber, memberId, planName, planType, effectiveDate, expirationDate, annualLimit, deductible,
                coPayPercentage, isPrimary, policyHolderName, policyHolderRelation, requiresPreAuth);
        return new ResponseEntity<>(insurance, HttpStatus.CREATED);
    }

    @PostMapping("/patients/insurance/{insuranceId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientInsurance> verifyInsurance(@PathVariable Long insuranceId) {
        return ResponseEntity.ok(insuranceService.verifyInsurance(insuranceId));
    }

    @GetMapping("/patients/{patientId}/insurance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<PatientInsurance>> getPatientInsurances(@PathVariable Long patientId) {
        return ResponseEntity.ok(insuranceService.getPatientInsurances(patientId));
    }

    @GetMapping("/patients/{patientId}/insurance/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<PatientInsurance>> getActivePatientInsurances(@PathVariable Long patientId) {
        return ResponseEntity.ok(insuranceService.getActivePatientInsurances(patientId));
    }

    @GetMapping("/patients/{patientId}/insurance/primary")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<PatientInsurance> getPrimaryInsurance(@PathVariable Long patientId) {
        PatientInsurance primary = insuranceService.getPrimaryInsurance(patientId);
        if (primary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(primary);
    }

    @PostMapping("/claims")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<InsuranceClaim> createClaim(@RequestParam Long invoiceId,
            @RequestParam Long patientInsuranceId, @RequestParam BigDecimal claimedAmount,
            @RequestParam(required = false) String diagnosisCodes,
            @RequestParam(required = false) String procedureCodes, @RequestParam(required = false) String preAuthNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preAuthDate,
            @RequestParam(required = false) BigDecimal preAuthAmount, @RequestParam(required = false) String notes) {

        Long hospitalId = getCurrentUserHospitalId();
        Long userId = getCurrentUserId();

        InsuranceClaim claim = insuranceService.createClaim(hospitalId, invoiceId, patientInsuranceId, claimedAmount,
                diagnosisCodes, procedureCodes, preAuthNumber, preAuthDate, preAuthAmount, userId, notes);
        return new ResponseEntity<>(claim, HttpStatus.CREATED);
    }

    @PostMapping("/claims/{claimId}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<InsuranceClaim> submitClaim(@PathVariable Long claimId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(insuranceService.submitClaim(claimId, userId));
    }

    @PostMapping("/claims/{claimId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<InsuranceClaim> approveClaim(@PathVariable Long claimId,
            @RequestParam BigDecimal approvedAmount, @RequestParam(required = false) String insuranceReferenceNumber) {
        Long userId = getCurrentUserId();
        return ResponseEntity
                .ok(insuranceService.approveClaim(claimId, approvedAmount, insuranceReferenceNumber, userId));
    }

    @PostMapping("/claims/{claimId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<InsuranceClaim> rejectClaim(@PathVariable Long claimId,
            @RequestParam String rejectionReason) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(insuranceService.rejectClaim(claimId, rejectionReason, userId));
    }

    @PostMapping("/claims/{claimId}/settle")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<InsuranceClaim> settleClaim(@PathVariable Long claimId,
            @RequestParam BigDecimal settledAmount) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(insuranceService.settleClaim(claimId, settledAmount, userId));
    }

    @PostMapping("/claims/{claimId}/appeal")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<InsuranceClaim> appealClaim(@PathVariable Long claimId, @RequestParam String appealReason) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(insuranceService.appealClaim(claimId, appealReason, userId));
    }

    @GetMapping("/claims/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<InsuranceClaim> getClaim(@PathVariable Long id) {
        return ResponseEntity.ok(insuranceService.getClaim(id));
    }

    @GetMapping("/claims/number/{claimNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<InsuranceClaim> getClaimByNumber(@PathVariable String claimNumber) {
        return ResponseEntity.ok(insuranceService.getClaimByNumber(claimNumber));
    }

    @GetMapping("/claims")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Page<InsuranceClaim>> getClaims(@RequestParam(required = false) InsuranceClaimStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(insuranceService.getClaims(hospitalId, status, pageable));
    }

    @GetMapping("/claims/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<InsuranceClaim>> getPendingClaims() {
        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(insuranceService.getPendingClaims(hospitalId));
    }

    @GetMapping("/claims/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<InsuranceClaim>> getClaimsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(insuranceService.getClaimsByPatient(patientId));
    }

    @GetMapping("/claims/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<InsuranceClaim>> getClaimsByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(insuranceService.getClaimsByInvoice(invoiceId));
    }

    @GetMapping("/reports/claims")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getClaimsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long hospitalId = getCurrentUserHospitalId();
        return ResponseEntity.ok(insuranceService.getClaimsReport(hospitalId, startDate, endDate));
    }

    private Long getCurrentUserHospitalId() {
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        if (currentUser.getHospital() == null) {
            throw new RuntimeException("User is not associated with a hospital");
        }
        return currentUser.getHospital().getId();
    }

    private Long getCurrentUserId() {
        return securityUtils.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
    }
}
