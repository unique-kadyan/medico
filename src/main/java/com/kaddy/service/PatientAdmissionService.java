package com.kaddy.service;

import com.kaddy.dto.PatientAdmissionDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.*;
import com.kaddy.model.enums.AdmissionStatus;
import com.kaddy.model.enums.BedStatus;
import com.kaddy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientAdmissionService {

    private final PatientAdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional
    public PatientAdmissionDTO createAdmission(Long hospitalId, PatientAdmissionDTO dto, Long admittedByUserId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        if (admissionRepository.existsByPatientIdAndStatus(dto.getPatientId(), AdmissionStatus.ADMITTED)) {
            throw new IllegalStateException("Patient is already admitted");
        }

        PatientAdmission admission = new PatientAdmission();
        admission.setAdmissionNumber(generateAdmissionNumber());
        admission.setPatient(patient);
        admission.setHospital(hospital);
        admission.setAdmissionDateTime(LocalDateTime.now());
        admission.setAdmissionType(dto.getAdmissionType());
        admission.setStatus(AdmissionStatus.ADMITTED);

        if (dto.getBedId() != null) {
            Bed bed = bedRepository.findById(dto.getBedId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
            if (bed.getStatus() != BedStatus.AVAILABLE) {
                throw new IllegalStateException("Selected bed is not available");
            }
            admission.setBed(bed);
            admission.setWard(bed.getWard());

            bed.setStatus(BedStatus.OCCUPIED);
            bed.setCurrentPatient(patient);
            bedRepository.save(bed);

            Ward ward = bed.getWard();
            ward.setAvailableBeds(ward.getAvailableBeds() - 1);
            wardRepository.save(ward);
        }

        if (dto.getAdmittingDoctorId() != null) {
            Doctor admittingDoctor = doctorRepository.findById(dto.getAdmittingDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admitting doctor not found"));
            admission.setAdmittingDoctor(admittingDoctor);
        }

        if (dto.getAttendingDoctorId() != null) {
            Doctor attendingDoctor = doctorRepository.findById(dto.getAttendingDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Attending doctor not found"));
            admission.setAttendingDoctor(attendingDoctor);
        }

        User admittedBy = userRepository.findById(admittedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        admission.setAdmittedBy(admittedBy);

        admission.setChiefComplaint(dto.getChiefComplaint());
        admission.setAdmissionDiagnosis(dto.getAdmissionDiagnosis());
        admission.setTreatmentPlan(dto.getTreatmentPlan());
        admission.setAllergies(dto.getAllergies());
        admission.setBloodGroup(dto.getBloodGroup());
        admission.setVitalSigns(dto.getVitalSigns());
        admission.setMedicalHistory(dto.getMedicalHistory());
        admission.setSurgicalHistory(dto.getSurgicalHistory());
        admission.setCurrentMedications(dto.getCurrentMedications());

        admission.setEmergencyContactName(dto.getEmergencyContactName());
        admission.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        admission.setEmergencyContactRelation(dto.getEmergencyContactRelation());

        admission.setInsuranceProvider(dto.getInsuranceProvider());
        admission.setInsurancePolicyNumber(dto.getInsurancePolicyNumber());
        admission.setHasInsurance(dto.getHasInsurance() != null ? dto.getHasInsurance() : false);

        admission.setEstimatedCost(dto.getEstimatedCost());
        admission.setDepositAmount(dto.getDepositAmount());

        admission.setIsEmergency(dto.getIsEmergency() != null ? dto.getIsEmergency() : false);
        admission.setRequiresIcu(dto.getRequiresIcu() != null ? dto.getRequiresIcu() : false);

        admission.setSpecialInstructions(dto.getSpecialInstructions());
        admission.setExpectedDischargeDate(dto.getExpectedDischargeDate());

        PatientAdmission saved = admissionRepository.save(admission);
        log.info("Created admission {} for patient {}", saved.getAdmissionNumber(), patient.getId());

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public PatientAdmissionDTO getAdmissionById(Long id) {
        PatientAdmission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));
        return mapToDTO(admission);
    }

    @Transactional(readOnly = true)
    public PatientAdmissionDTO getAdmissionByNumber(String admissionNumber) {
        PatientAdmission admission = admissionRepository.findByAdmissionNumber(admissionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));
        return mapToDTO(admission);
    }

    @Transactional(readOnly = true)
    public List<PatientAdmissionDTO> getCurrentAdmissions(Long hospitalId) {
        return admissionRepository.findCurrentAdmissions(hospitalId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientAdmissionDTO> getPatientAdmissionHistory(Long patientId) {
        return admissionRepository.findByPatientIdOrderByAdmissionDateTimeDesc(patientId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientAdmissionDTO> getAdmissionsByWard(Long wardId) {
        return admissionRepository.findCurrentAdmissionsByWard(wardId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientAdmissionDTO> getAdmissionsByDoctor(Long doctorId) {
        return admissionRepository.findCurrentAdmissionsByDoctor(doctorId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientAdmissionDTO dischargePatient(Long admissionId, PatientAdmissionDTO dischargeInfo,
            Long dischargedByUserId) {
        PatientAdmission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));

        if (admission.getStatus() != AdmissionStatus.ADMITTED) {
            throw new IllegalStateException("Patient is not currently admitted");
        }

        admission.setStatus(AdmissionStatus.DISCHARGED);
        admission.setActualDischargeDateTime(LocalDateTime.now());
        admission.setDischargeDiagnosis(dischargeInfo.getDischargeDiagnosis());
        admission.setDischargeNotes(dischargeInfo.getDischargeNotes());
        admission.setDischargeMedications(dischargeInfo.getDischargeMedications());
        admission.setFollowUpInstructions(dischargeInfo.getFollowUpInstructions());
        admission.setNextFollowUpDate(dischargeInfo.getNextFollowUpDate());

        User dischargedBy = userRepository.findById(dischargedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        admission.setDischargedBy(dischargedBy);

        if (admission.getBed() != null) {
            Bed bed = admission.getBed();
            bed.setStatus(BedStatus.CLEANING);
            bed.setCurrentPatient(null);
            bedRepository.save(bed);
        }

        PatientAdmission saved = admissionRepository.save(admission);
        log.info("Discharged patient from admission {}", admission.getAdmissionNumber());

        return mapToDTO(saved);
    }

    @Transactional
    public PatientAdmissionDTO transferBed(Long admissionId, Long newBedId) {
        PatientAdmission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));

        Bed newBed = bedRepository.findById(newBedId)
                .orElseThrow(() -> new ResourceNotFoundException("New bed not found"));

        if (newBed.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalStateException("New bed is not available");
        }

        if (admission.getBed() != null) {
            Bed oldBed = admission.getBed();
            oldBed.setStatus(BedStatus.CLEANING);
            oldBed.setCurrentPatient(null);
            bedRepository.save(oldBed);

            Ward oldWard = oldBed.getWard();
            oldWard.setAvailableBeds(oldWard.getAvailableBeds() + 1);
            wardRepository.save(oldWard);
        }

        newBed.setStatus(BedStatus.OCCUPIED);
        newBed.setCurrentPatient(admission.getPatient());
        bedRepository.save(newBed);

        Ward newWard = newBed.getWard();
        newWard.setAvailableBeds(newWard.getAvailableBeds() - 1);
        wardRepository.save(newWard);

        admission.setBed(newBed);
        admission.setWard(newWard);

        log.info("Transferred admission {} to bed {}", admission.getAdmissionNumber(), newBed.getBedNumber());
        return mapToDTO(admissionRepository.save(admission));
    }

    private String generateAdmissionNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ADM-" + datePart + "-" + uniquePart;
    }

    private PatientAdmissionDTO mapToDTO(PatientAdmission admission) {
        PatientAdmissionDTO dto = new PatientAdmissionDTO();
        dto.setId(admission.getId());
        dto.setAdmissionNumber(admission.getAdmissionNumber());

        if (admission.getPatient() != null) {
            dto.setPatientId(admission.getPatient().getId());
            dto.setPatientName(admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName());
            dto.setPatientPhone(admission.getPatient().getPhone());
            dto.setPatientEmail(admission.getPatient().getEmail());
        }

        if (admission.getHospital() != null) {
            dto.setHospitalId(admission.getHospital().getId());
            dto.setHospitalName(admission.getHospital().getName());
        }

        if (admission.getBed() != null) {
            dto.setBedId(admission.getBed().getId());
            dto.setBedNumber(admission.getBed().getBedNumber());
        }

        if (admission.getWard() != null) {
            dto.setWardId(admission.getWard().getId());
            dto.setWardName(admission.getWard().getName());
        }

        if (admission.getAdmittingDoctor() != null) {
            dto.setAdmittingDoctorId(admission.getAdmittingDoctor().getId());
            dto.setAdmittingDoctorName("Dr. " + admission.getAdmittingDoctor().getUser().getFirstName() + " "
                    + admission.getAdmittingDoctor().getUser().getLastName());
        }

        if (admission.getAttendingDoctor() != null) {
            dto.setAttendingDoctorId(admission.getAttendingDoctor().getId());
            dto.setAttendingDoctorName("Dr. " + admission.getAttendingDoctor().getUser().getFirstName() + " "
                    + admission.getAttendingDoctor().getUser().getLastName());
        }

        dto.setAdmissionType(admission.getAdmissionType());
        dto.setStatus(admission.getStatus());
        dto.setAdmissionDateTime(admission.getAdmissionDateTime());
        dto.setExpectedDischargeDate(admission.getExpectedDischargeDate());
        dto.setActualDischargeDateTime(admission.getActualDischargeDateTime());
        dto.setChiefComplaint(admission.getChiefComplaint());
        dto.setAdmissionDiagnosis(admission.getAdmissionDiagnosis());
        dto.setDischargeDiagnosis(admission.getDischargeDiagnosis());
        dto.setTreatmentPlan(admission.getTreatmentPlan());
        dto.setAllergies(admission.getAllergies());
        dto.setBloodGroup(admission.getBloodGroup());
        dto.setVitalSigns(admission.getVitalSigns());
        dto.setMedicalHistory(admission.getMedicalHistory());
        dto.setSurgicalHistory(admission.getSurgicalHistory());
        dto.setCurrentMedications(admission.getCurrentMedications());
        dto.setEmergencyContactName(admission.getEmergencyContactName());
        dto.setEmergencyContactPhone(admission.getEmergencyContactPhone());
        dto.setEmergencyContactRelation(admission.getEmergencyContactRelation());
        dto.setInsuranceProvider(admission.getInsuranceProvider());
        dto.setInsurancePolicyNumber(admission.getInsurancePolicyNumber());
        dto.setEstimatedCost(admission.getEstimatedCost());
        dto.setDepositAmount(admission.getDepositAmount());
        dto.setDischargeNotes(admission.getDischargeNotes());
        dto.setDischargeMedications(admission.getDischargeMedications());
        dto.setFollowUpInstructions(admission.getFollowUpInstructions());
        dto.setNextFollowUpDate(admission.getNextFollowUpDate());
        dto.setSpecialInstructions(admission.getSpecialInstructions());
        dto.setIsEmergency(admission.getIsEmergency());
        dto.setRequiresIcu(admission.getRequiresIcu());
        dto.setHasInsurance(admission.getHasInsurance());
        dto.setCreatedAt(admission.getCreatedAt());
        dto.setUpdatedAt(admission.getUpdatedAt());
        dto.setActive(admission.getActive());

        if (admission.getAdmittedBy() != null) {
            dto.setAdmittedById(admission.getAdmittedBy().getId());
            dto.setAdmittedByName(
                    admission.getAdmittedBy().getFirstName() + " " + admission.getAdmittedBy().getLastName());
        }

        if (admission.getDischargedBy() != null) {
            dto.setDischargedById(admission.getDischargedBy().getId());
            dto.setDischargedByName(
                    admission.getDischargedBy().getFirstName() + " " + admission.getDischargedBy().getLastName());
        }

        return dto;
    }
}
