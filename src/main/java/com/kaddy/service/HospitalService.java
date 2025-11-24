package com.kaddy.service;

import com.kaddy.dto.HospitalDTO;
import com.kaddy.dto.HospitalRegistrationRequest;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.exception.SubscriptionException;
import com.kaddy.model.Hospital;
import com.kaddy.model.User;
import com.kaddy.model.enums.SubscriptionPlan;
import com.kaddy.model.enums.SubscriptionStatus;
import com.kaddy.model.enums.UserRole;
import com.kaddy.repository.HospitalRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    private static final int TRIAL_DAYS = 10;

    @Transactional
    public HospitalDTO registerHospital(HospitalRegistrationRequest request) {
        log.info("Registering new hospital: {}", request.getHospitalName());

        if (hospitalRepository.existsByEmail(request.getHospitalEmail())) {
            throw new IllegalArgumentException("Hospital with this email already exists");
        }

        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        String hospitalCode = generateHospitalCode(request.getHospitalName());
        while (hospitalRepository.existsByCode(hospitalCode)) {
            hospitalCode = generateHospitalCode(request.getHospitalName());
        }

        Hospital hospital = new Hospital();
        hospital.setName(request.getHospitalName());
        hospital.setCode(hospitalCode);
        hospital.setEmail(request.getHospitalEmail());
        hospital.setPhone(request.getHospitalPhone());
        hospital.setAddress(request.getHospitalAddress());
        hospital.setCity(request.getCity());
        hospital.setState(request.getState());
        hospital.setCountry(request.getCountry());
        hospital.setPostalCode(request.getPostalCode());
        hospital.setRegistrationNumber(request.getRegistrationNumber());
        hospital.setTaxId(request.getTaxId());
        hospital.setWebsite(request.getWebsite());

        hospital.setSubscriptionPlan(SubscriptionPlan.TRIAL);
        hospital.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        hospital.setTrialStartDate(LocalDateTime.now());
        hospital.setTrialEndDate(LocalDateTime.now().plusDays(TRIAL_DAYS));

        hospital.setMaxUsers(SubscriptionPlan.TRIAL.getMaxUsers());
        hospital.setMaxPatients(SubscriptionPlan.TRIAL.getMaxPatients());
        hospital.setAiEnabled(SubscriptionPlan.TRIAL.isAiEnabled());
        hospital.setFhirEnabled(SubscriptionPlan.TRIAL.isFhirEnabled());

        Hospital savedHospital = hospitalRepository.save(hospital);

        User adminUser = new User();
        adminUser.setUsername(request.getAdminEmail());
        adminUser.setEmail(request.getAdminEmail());
        adminUser.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        adminUser.setFirstName(request.getAdminFirstName());
        adminUser.setLastName(request.getAdminLastName());
        adminUser.setPhone(request.getAdminPhone());
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setEnabled(true);
        adminUser.setHospital(savedHospital);
        adminUser.setIsHospitalAdmin(true);

        userRepository.save(adminUser);

        log.info("Hospital registered successfully with code: {}", hospitalCode);
        return mapToDTO(savedHospital);
    }

    @Transactional(readOnly = true)
    public HospitalDTO getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
        return mapToDTO(hospital);
    }

    @Transactional(readOnly = true)
    public HospitalDTO getHospitalByCode(String code) {
        Hospital hospital = hospitalRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with code: " + code));
        return mapToDTO(hospital);
    }

    @Transactional(readOnly = true)
    public List<HospitalDTO> getAllHospitals() {
        return hospitalRepository.findByActiveTrue().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public HospitalDTO updateHospital(Long id, HospitalDTO dto) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));

        hospital.setName(dto.getName());
        hospital.setPhone(dto.getPhone());
        hospital.setAddress(dto.getAddress());
        hospital.setCity(dto.getCity());
        hospital.setState(dto.getState());
        hospital.setCountry(dto.getCountry());
        hospital.setPostalCode(dto.getPostalCode());
        hospital.setRegistrationNumber(dto.getRegistrationNumber());
        hospital.setTaxId(dto.getTaxId());
        hospital.setWebsite(dto.getWebsite());
        hospital.setLogoUrl(dto.getLogoUrl());

        return mapToDTO(hospitalRepository.save(hospital));
    }

    @Transactional
    public HospitalDTO upgradePlan(Long hospitalId, SubscriptionPlan newPlan) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + hospitalId));

        if (newPlan == SubscriptionPlan.TRIAL) {
            throw new IllegalArgumentException("Cannot upgrade to trial plan");
        }

        hospital.setSubscriptionPlan(newPlan);
        hospital.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        hospital.setSubscriptionStartDate(LocalDateTime.now());
        hospital.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));

        hospital.setMaxUsers(newPlan.getMaxUsers());
        hospital.setMaxPatients(newPlan.getMaxPatients());
        hospital.setAiEnabled(newPlan.isAiEnabled());
        hospital.setFhirEnabled(newPlan.isFhirEnabled());

        log.info("Hospital {} upgraded to {} plan", hospitalId, newPlan);
        return mapToDTO(hospitalRepository.save(hospital));
    }

    @Transactional(readOnly = true)
    public void validateSubscription(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + hospitalId));

        if (!hospital.isSubscriptionActive()) {
            throw new SubscriptionException("Hospital subscription has expired. Please upgrade your plan.");
        }
    }

    @Transactional(readOnly = true)
    public void validateUserLimit(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + hospitalId));

        long currentUsers = hospitalRepository.countActiveUsersByHospitalId(hospitalId);
        if (hospital.getMaxUsers() != -1 && currentUsers >= hospital.getMaxUsers()) {
            throw new SubscriptionException("User limit reached. Please upgrade your plan to add more users.");
        }
    }

    @Transactional(readOnly = true)
    public void validateFeatureAccess(Long hospitalId, String feature) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + hospitalId));

        switch (feature.toLowerCase()) {
            case "ai" :
                if (!hospital.getAiEnabled()) {
                    throw new SubscriptionException(
                            "AI features are not available in your current plan. Please upgrade to Professional or Enterprise.");
                }
                break;
            case "fhir" :
                if (!hospital.getFhirEnabled()) {
                    throw new SubscriptionException(
                            "FHIR/EHR features are not available in your current plan. Please upgrade to Basic or higher.");
                }
                break;
            case "bed_management" :
                if (!hospital.getBedManagementEnabled()) {
                    throw new SubscriptionException("Bed Management is not available in your current plan.");
                }
                break;
            default :
                break;
        }
    }

    @Transactional
    public void processExpiredTrials() {
        List<Hospital> expiredTrials = hospitalRepository.findExpiredTrials(LocalDateTime.now());
        for (Hospital hospital : expiredTrials) {
            hospital.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            hospitalRepository.save(hospital);
            log.info("Trial expired for hospital: {}", hospital.getCode());
        }
    }

    @Transactional
    public HospitalDTO updateHospitalLogo(Long hospitalId, String logoUrl) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + hospitalId));

        hospital.setLogoUrl(logoUrl);
        Hospital savedHospital = hospitalRepository.save(hospital);
        log.info("Logo updated for hospital {}: {}", hospitalId, logoUrl);
        return mapToDTO(savedHospital);
    }

    private String generateHospitalCode(String hospitalName) {
        String prefix = hospitalName.replaceAll("[^a-zA-Z]", "").toUpperCase();
        prefix = prefix.length() > 3 ? prefix.substring(0, 3) : prefix;
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + suffix;
    }

    private HospitalDTO mapToDTO(Hospital hospital) {
        HospitalDTO dto = modelMapper.map(hospital, HospitalDTO.class);
        dto.setDaysRemainingInTrial(hospital.getDaysRemainingInTrial());
        dto.setIsTrialExpired(hospital.isTrialExpired());
        dto.setIsSubscriptionActive(hospital.isSubscriptionActive());
        dto.setCurrentUserCount(hospitalRepository.countActiveUsersByHospitalId(hospital.getId()));
        return dto;
    }
}
