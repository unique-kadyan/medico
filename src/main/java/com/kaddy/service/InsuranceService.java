package com.kaddy.service;

import com.kaddy.model.*;
import com.kaddy.model.enums.InsuranceClaimStatus;
import com.kaddy.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InsuranceService {

    private final InsuranceProviderRepository insuranceProviderRepository;
    private final PatientInsuranceRepository patientInsuranceRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public InsuranceProvider createProvider(String providerCode, String name, String address, String phone,
            String email, String contactPerson, String tpaName, String tpaCode, BigDecimal defaultCoveragePercentage,
            Integer claimProcessingDays) {
        if (insuranceProviderRepository.existsByProviderCode(providerCode)) {
            throw new IllegalArgumentException("Provider code already exists: " + providerCode);
        }

        InsuranceProvider provider = new InsuranceProvider();
        provider.setProviderCode(providerCode);
        provider.setName(name);
        provider.setAddress(address);
        provider.setPhone(phone);
        provider.setEmail(email);
        provider.setContactPerson(contactPerson);
        provider.setTpaName(tpaName);
        provider.setTpaCode(tpaCode);
        provider.setDefaultCoveragePercentage(
                defaultCoveragePercentage != null ? defaultCoveragePercentage : BigDecimal.valueOf(80));
        provider.setClaimProcessingDays(claimProcessingDays != null ? claimProcessingDays : 30);
        provider.setIsActive(true);

        log.info("Created insurance provider: {} (Code: {})", name, providerCode);
        return insuranceProviderRepository.save(provider);
    }

    public InsuranceProvider updateProvider(Long providerId, String name, String phone, String email,
            String contactPerson, BigDecimal defaultCoveragePercentage, Boolean isActive) {
        InsuranceProvider provider = insuranceProviderRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Insurance provider not found"));

        if (name != null)
            provider.setName(name);
        if (phone != null)
            provider.setPhone(phone);
        if (email != null)
            provider.setEmail(email);
        if (contactPerson != null)
            provider.setContactPerson(contactPerson);
        if (defaultCoveragePercentage != null)
            provider.setDefaultCoveragePercentage(defaultCoveragePercentage);
        if (isActive != null)
            provider.setIsActive(isActive);

        return insuranceProviderRepository.save(provider);
    }

    @Transactional(readOnly = true)
    public InsuranceProvider getProvider(Long providerId) {
        return insuranceProviderRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Insurance provider not found"));
    }

    @Transactional(readOnly = true)
    public List<InsuranceProvider> getActiveProviders() {
        return insuranceProviderRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Page<InsuranceProvider> searchProviders(String search, Pageable pageable) {
        return insuranceProviderRepository.searchProviders(search, pageable);
    }

    public PatientInsurance addPatientInsurance(Long patientId, Long providerId, String policyNumber,
            String groupNumber, String memberId, String planName, String planType, LocalDate effectiveDate,
            LocalDate expirationDate, BigDecimal annualLimit, BigDecimal deductible, BigDecimal coPayPercentage,
            Boolean isPrimary, String policyHolderName, String policyHolderRelation, Boolean requiresPreAuth) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
        InsuranceProvider provider = insuranceProviderRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Insurance provider not found"));

        PatientInsurance insurance = new PatientInsurance();
        insurance.setPatient(patient);
        insurance.setProvider(provider);
        insurance.setPolicyNumber(policyNumber);
        insurance.setGroupNumber(groupNumber);
        insurance.setMemberId(memberId);
        insurance.setPlanName(planName);
        insurance.setPlanType(planType);
        insurance.setEffectiveDate(effectiveDate);
        insurance.setExpirationDate(expirationDate);
        insurance.setAnnualLimit(annualLimit);
        insurance.setRemainingLimit(annualLimit);
        insurance.setDeductible(deductible);
        insurance.setDeductibleMet(BigDecimal.ZERO);
        insurance.setCoPayPercentage(coPayPercentage);
        insurance.setIsPrimary(isPrimary != null ? isPrimary : true);
        insurance.setPolicyHolderName(policyHolderName);
        insurance.setPolicyHolderRelation(policyHolderRelation);
        insurance.setRequiresPreAuth(requiresPreAuth != null ? requiresPreAuth : false);
        insurance.setIsVerified(false);

        if (Boolean.TRUE.equals(isPrimary)) {
            List<PatientInsurance> existingInsurances = patientInsuranceRepository.findByPatientId(patientId);
            for (PatientInsurance existing : existingInsurances) {
                if (Boolean.TRUE.equals(existing.getIsPrimary())) {
                    existing.setIsPrimary(false);
                    patientInsuranceRepository.save(existing);
                }
            }
        }

        log.info("Added insurance for patient {}: {} - {}", patientId, provider.getName(), policyNumber);
        return patientInsuranceRepository.save(insurance);
    }

    public PatientInsurance verifyInsurance(Long insuranceId) {
        PatientInsurance insurance = patientInsuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new EntityNotFoundException("Patient insurance not found"));

        insurance.setIsVerified(true);
        insurance.setLastVerifiedDate(LocalDate.now());

        log.info("Verified insurance: {}", insurance.getPolicyNumber());
        return patientInsuranceRepository.save(insurance);
    }

    @Transactional(readOnly = true)
    public List<PatientInsurance> getPatientInsurances(Long patientId) {
        return patientInsuranceRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<PatientInsurance> getActivePatientInsurances(Long patientId) {
        return patientInsuranceRepository.findActiveInsuranceByPatient(patientId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public PatientInsurance getPrimaryInsurance(Long patientId) {
        return patientInsuranceRepository.findPrimaryInsurance(patientId, LocalDate.now()).orElse(null);
    }

    public InsuranceClaim createClaim(Long hospitalId, Long invoiceId, Long patientInsuranceId,
            BigDecimal claimedAmount, String diagnosisCodes, String procedureCodes, String preAuthNumber,
            LocalDate preAuthDate, BigDecimal preAuthAmount, Long userId, String notes) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new EntityNotFoundException("Hospital not found"));
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        PatientInsurance patientInsurance = patientInsuranceRepository.findById(patientInsuranceId)
                .orElseThrow(() -> new EntityNotFoundException("Patient insurance not found"));

        InsuranceClaim claim = new InsuranceClaim();
        claim.setClaimNumber(generateClaimNumber(hospitalId));
        claim.setHospital(hospital);
        claim.setPatient(invoice.getPatient());
        claim.setPatientInsurance(patientInsurance);
        claim.setInsuranceProvider(patientInsurance.getProvider());
        claim.setInvoice(invoice);
        claim.setStatus(InsuranceClaimStatus.DRAFT);
        claim.setClaimDate(LocalDate.now());
        claim.setClaimedAmount(claimedAmount);
        claim.setDiagnosisCodes(diagnosisCodes);
        claim.setProcedureCodes(procedureCodes);
        claim.setPreAuthNumber(preAuthNumber);
        claim.setPreAuthDate(preAuthDate);
        claim.setPreAuthAmount(preAuthAmount);
        claim.setNotes(notes);

        log.info("Created insurance claim: {} for invoice {}", claim.getClaimNumber(), invoice.getInvoiceNumber());
        return insuranceClaimRepository.save(claim);
    }

    public InsuranceClaim submitClaim(Long claimId, Long userId) {
        InsuranceClaim claim = insuranceClaimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        if (claim.getStatus() != InsuranceClaimStatus.DRAFT) {
            throw new IllegalStateException("Only draft claims can be submitted");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        claim.setStatus(InsuranceClaimStatus.SUBMITTED);
        claim.setSubmissionDate(LocalDate.now());
        claim.setSubmittedBy(user);
        claim.setSubmittedAt(LocalDateTime.now());

        log.info("Submitted insurance claim: {}", claim.getClaimNumber());
        return insuranceClaimRepository.save(claim);
    }

    public InsuranceClaim updateClaimStatus(Long claimId, InsuranceClaimStatus status, BigDecimal approvedAmount,
            BigDecimal rejectedAmount, BigDecimal deductionAmount, String insuranceReferenceNumber,
            String rejectionReason, String deductionReason, Long userId) {
        InsuranceClaim claim = insuranceClaimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        claim.setStatus(status);
        claim.setResponseDate(LocalDate.now());

        if (approvedAmount != null)
            claim.setApprovedAmount(approvedAmount);
        if (rejectedAmount != null)
            claim.setRejectedAmount(rejectedAmount);
        if (deductionAmount != null)
            claim.setDeductionAmount(deductionAmount);
        if (insuranceReferenceNumber != null)
            claim.setInsuranceReferenceNumber(insuranceReferenceNumber);
        if (rejectionReason != null)
            claim.setRejectionReason(rejectionReason);
        if (deductionReason != null)
            claim.setDeductionReason(deductionReason);

        if (user != null) {
            claim.setProcessedBy(user);
            claim.setProcessedAt(LocalDateTime.now());
        }

        log.info("Updated claim {} status to {}", claim.getClaimNumber(), status);
        return insuranceClaimRepository.save(claim);
    }

    public InsuranceClaim approveClaim(Long claimId, BigDecimal approvedAmount, String insuranceReferenceNumber,
            Long userId) {
        InsuranceClaim claim = insuranceClaimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        BigDecimal rejected = claim.getClaimedAmount().subtract(approvedAmount);

        claim.setStatus(InsuranceClaimStatus.APPROVED);
        claim.setApprovedAmount(approvedAmount);
        claim.setRejectedAmount(rejected.compareTo(BigDecimal.ZERO) > 0 ? rejected : BigDecimal.ZERO);
        claim.setInsuranceReferenceNumber(insuranceReferenceNumber);
        claim.setResponseDate(LocalDate.now());

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            claim.setProcessedBy(user);
            claim.setProcessedAt(LocalDateTime.now());
        }

        log.info("Approved claim {}: {} of {}", claim.getClaimNumber(), approvedAmount, claim.getClaimedAmount());
        return insuranceClaimRepository.save(claim);
    }

    public InsuranceClaim rejectClaim(Long claimId, String rejectionReason, Long userId) {
        InsuranceClaim claim = insuranceClaimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        claim.setStatus(InsuranceClaimStatus.REJECTED);
        claim.setRejectedAmount(claim.getClaimedAmount());
        claim.setApprovedAmount(BigDecimal.ZERO);
        claim.setRejectionReason(rejectionReason);
        claim.setResponseDate(LocalDate.now());

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            claim.setProcessedBy(user);
            claim.setProcessedAt(LocalDateTime.now());
        }

        log.info("Rejected claim {}: {}", claim.getClaimNumber(), rejectionReason);
        return insuranceClaimRepository.save(claim);
    }

    public InsuranceClaim settleClaim(Long claimId, BigDecimal settledAmount, Long userId) {
        InsuranceClaim claim = insuranceClaimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        if (claim.getStatus() != InsuranceClaimStatus.APPROVED
                && claim.getStatus() != InsuranceClaimStatus.PARTIALLY_APPROVED) {
            throw new IllegalStateException("Only approved claims can be settled");
        }

        claim.setStatus(InsuranceClaimStatus.SETTLED);
        claim.setSettledAmount(settledAmount);
        claim.setSettlementDate(LocalDate.now());

        PatientInsurance patientInsurance = claim.getPatientInsurance();
        if (patientInsurance.getRemainingLimit() != null) {
            BigDecimal newLimit = patientInsurance.getRemainingLimit().subtract(settledAmount);
            patientInsurance.setRemainingLimit(newLimit.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newLimit);
            patientInsuranceRepository.save(patientInsurance);
        }

        log.info("Settled claim {}: {}", claim.getClaimNumber(), settledAmount);
        return insuranceClaimRepository.save(claim);
    }

    public InsuranceClaim appealClaim(Long claimId, String appealReason, Long userId) {
        InsuranceClaim claim = insuranceClaimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        if (claim.getStatus() != InsuranceClaimStatus.REJECTED
                && claim.getStatus() != InsuranceClaimStatus.PARTIALLY_APPROVED) {
            throw new IllegalStateException("Only rejected or partially approved claims can be appealed");
        }

        claim.setStatus(InsuranceClaimStatus.APPEALED);
        claim.setIsAppealed(true);
        claim.setAppealReason(appealReason);
        claim.setAppealDate(LocalDate.now());
        claim.setAppealStatus(InsuranceClaimStatus.SUBMITTED);

        log.info("Appealed claim {}: {}", claim.getClaimNumber(), appealReason);
        return insuranceClaimRepository.save(claim);
    }

    @Transactional(readOnly = true)
    public InsuranceClaim getClaim(Long claimId) {
        return insuranceClaimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));
    }

    @Transactional(readOnly = true)
    public InsuranceClaim getClaimByNumber(String claimNumber) {
        return insuranceClaimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));
    }

    @Transactional(readOnly = true)
    public Page<InsuranceClaim> getClaims(Long hospitalId, InsuranceClaimStatus status, Pageable pageable) {
        if (status != null) {
            return insuranceClaimRepository.findByHospitalIdAndStatus(hospitalId, status, pageable);
        }
        return insuranceClaimRepository.findByHospitalId(hospitalId, pageable);
    }

    @Transactional(readOnly = true)
    public List<InsuranceClaim> getClaimsByPatient(Long patientId) {
        return insuranceClaimRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<InsuranceClaim> getClaimsByInvoice(Long invoiceId) {
        return insuranceClaimRepository.findByInvoiceId(invoiceId);
    }

    @Transactional(readOnly = true)
    public List<InsuranceClaim> getPendingClaims(Long hospitalId) {
        return insuranceClaimRepository.findPendingClaims(hospitalId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getClaimsReport(Long hospitalId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        BigDecimal totalClaimed = insuranceClaimRepository.getTotalClaimedAmount(hospitalId, startDate, endDate);
        BigDecimal totalSettled = insuranceClaimRepository.getTotalSettledAmount(hospitalId, startDate, endDate);
        BigDecimal totalRejected = insuranceClaimRepository.getTotalRejectedAmount(hospitalId, startDate, endDate);

        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalClaimedAmount", totalClaimed != null ? totalClaimed : BigDecimal.ZERO);
        report.put("totalSettledAmount", totalSettled != null ? totalSettled : BigDecimal.ZERO);
        report.put("totalRejectedAmount", totalRejected != null ? totalRejected : BigDecimal.ZERO);
        report.put("pendingClaims", insuranceClaimRepository.findPendingClaims(hospitalId).size());

        BigDecimal settlementRate = BigDecimal.ZERO;
        if (totalClaimed != null && totalClaimed.compareTo(BigDecimal.ZERO) > 0 && totalSettled != null) {
            settlementRate = totalSettled.multiply(BigDecimal.valueOf(100)).divide(totalClaimed, 2,
                    java.math.RoundingMode.HALF_UP);
        }
        report.put("settlementRate", settlementRate);

        return report;
    }

    private String generateClaimNumber(Long hospitalId) {
        String prefix = "CLM" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String lastNumber = insuranceClaimRepository.findLastClaimNumber(hospitalId, prefix);

        int sequence = 1;
        if (lastNumber != null) {
            String seqStr = lastNumber.substring(prefix.length());
            sequence = Integer.parseInt(seqStr) + 1;
        }

        return prefix + String.format("%04d", sequence);
    }
}
