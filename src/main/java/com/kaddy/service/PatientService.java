package com.kaddy.service;

import com.kaddy.dto.PatientDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Patient;
import com.kaddy.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;
    private final DoctorPatientAssignmentService assignmentService;
    private final com.kaddy.security.SecurityUtils securityUtils;

    @Cacheable(value = "patients", key = "#id")
    public PatientDTO getPatientById(Long id) {
        log.info("Fetching patient with ID: {}", id);
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
        return convertToDTO(patient);
    }

    @Cacheable(value = "patients", key = "#patientId")
    public PatientDTO getPatientByPatientId(String patientId) {
        log.info("Fetching patient with Patient ID: {}", patientId);
        Patient patient = patientRepository.findByPatientId(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with Patient ID: " + patientId));
        return convertToDTO(patient);
    }

    public List<PatientDTO> getAllPatients() {
        log.info("Fetching all patients");

        // If user is ADMIN, return all patients
        if (securityUtils.isAdmin()) {
            return patientRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        }

        // If user is DOCTOR, return only assigned patients
        if (securityUtils.isDoctor()) {
            Long doctorId = securityUtils.getCurrentDoctorId().orElse(null);
            if (doctorId != null) {
                List<Long> patientIds = assignmentService.getPatientIdsForDoctor(doctorId);
                return patientRepository.findAllById(patientIds)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            }
        }

        // For other roles (NURSE, PHARMACIST), return empty list or implement custom logic
        log.warn("User with role {} attempted to access all patients",
                securityUtils.getCurrentUserRole().orElse(null));
        return List.of();
    }

    public List<PatientDTO> getAllActivePatients() {
        log.info("Fetching all active patients");
        return patientRepository.findAllActivePatients()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<PatientDTO> searchPatientsByName(String name) {
        log.info("Searching patients by name: {}", name);
        return patientRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @CacheEvict(value = "patients", allEntries = true)
    public PatientDTO createPatient(PatientDTO patientDTO) {
        log.info("Creating new patient with ID: {}", patientDTO.getPatientId());

        if (patientRepository.existsByPatientId(patientDTO.getPatientId())) {
            throw new IllegalArgumentException("Patient with ID " + patientDTO.getPatientId() + " already exists");
        }

        Patient patient = convertToEntity(patientDTO);
        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created successfully with ID: {}", savedPatient.getId());

        return convertToDTO(savedPatient);
    }

    @CacheEvict(value = "patients", allEntries = true)
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        log.info("Updating patient with ID: {}", id);

        Patient existingPatient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));

        // Update fields
        existingPatient.setFirstName(patientDTO.getFirstName());
        existingPatient.setLastName(patientDTO.getLastName());
        existingPatient.setDateOfBirth(patientDTO.getDateOfBirth());
        existingPatient.setGender(patientDTO.getGender());
        existingPatient.setPhone(patientDTO.getPhone());
        existingPatient.setEmail(patientDTO.getEmail());
        existingPatient.setAddress(patientDTO.getAddress());
        existingPatient.setEmergencyContact(patientDTO.getEmergencyContact());
        existingPatient.setEmergencyContactPhone(patientDTO.getEmergencyContactPhone());
        existingPatient.setBloodGroup(patientDTO.getBloodGroup());
        existingPatient.setAllergies(patientDTO.getAllergies());
        existingPatient.setChronicConditions(patientDTO.getChronicConditions());

        Patient updatedPatient = patientRepository.save(existingPatient);
        log.info("Patient updated successfully with ID: {}", updatedPatient.getId());

        return convertToDTO(updatedPatient);
    }

    @CacheEvict(value = "patients", allEntries = true)
    public void deletePatient(Long id) {
        log.info("Deleting patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));

        patient.setActive(false);
        patientRepository.save(patient);
        log.info("Patient deactivated successfully with ID: {}", id);
    }

    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = modelMapper.map(patient, PatientDTO.class);
        dto.setAge(patient.getAge());
        dto.setFullName(patient.getFullName());
        return dto;
    }

    private Patient convertToEntity(PatientDTO dto) {
        return modelMapper.map(dto, Patient.class);
    }
}
