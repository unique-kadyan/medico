package com.kaddy.service.async;

import com.kaddy.dto.PatientDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.functional.FunctionalUtils;
import com.kaddy.model.Patient;
import com.kaddy.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncPatientService {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;
    private final Executor taskExecutor;

    @Async("taskExecutor")
    public CompletableFuture<PatientDTO> getPatientByIdAsync(Long id) {
        log.info("Async: Fetching patient with ID: {}", id);

        return CompletableFuture.supplyAsync(() -> {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
            return convertToDTO(patient);
        }, taskExecutor);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<PatientDTO>> getAllPatientsAsync() {
        log.info("Async: Fetching all patients");

        return CompletableFuture.supplyAsync(
                () -> patientRepository.findAll().parallelStream().map(this::convertToDTO).collect(Collectors.toList()),
                taskExecutor);
    }

    @Async("batchExecutor")
    @Transactional
    public CompletableFuture<List<PatientDTO>> createPatientsAsync(List<PatientDTO> patientDTOs) {
        log.info("Async: Creating {} patients", patientDTOs.size());

        return CompletableFuture.supplyAsync(() -> {
            List<Patient> patients = patientDTOs.parallelStream().peek(dto -> {
                if (patientRepository.existsByPatientId(dto.getPatientId())) {
                    throw new IllegalArgumentException("Patient with ID " + dto.getPatientId() + " already exists");
                }
            }).map(this::convertToEntity).collect(Collectors.toList());

            List<Patient> savedPatients = patientRepository.saveAll(patients);

            log.info("Successfully created {} patients", savedPatients.size());

            return savedPatients.stream().map(this::convertToDTO).collect(Collectors.toList());
        }, taskExecutor);
    }

    @Async("taskExecutor")
    public CompletableFuture<List<PatientDTO>> searchPatientsAsync(String name, String bloodGroup) {
        log.info("Async: Searching patients by name: {} and blood group: {}", name, bloodGroup);

        return CompletableFuture.supplyAsync(() -> {
            List<Patient> allPatients = patientRepository.findAll();

            return FunctionalUtils.filterAndTransform(allPatients, patient -> (name == null
                    || patient.getFullName().toLowerCase().contains(name.toLowerCase()))
                    && (bloodGroup == null
                            || patient.getBloodGroup() != null && patient.getBloodGroup().name().equals(bloodGroup)),
                    this::convertToDTO);
        }, taskExecutor);
    }

    @Async("batchExecutor")
    @Transactional
    public CompletableFuture<List<PatientDTO>> updatePatientsAsync(List<PatientDTO> updates) {
        log.info("Async: Updating {} patients", updates.size());

        return CompletableFuture.supplyAsync(() -> {
            List<CompletableFuture<PatientDTO>> updateFutures = updates.stream()
                    .map(dto -> updatePatientAsync(dto.getId(), dto)).collect(Collectors.toList());

            return FunctionalUtils.waitAll(updateFutures).join();
        }, taskExecutor);
    }

    private CompletableFuture<PatientDTO> updatePatientAsync(Long id, PatientDTO patientDTO) {
        return CompletableFuture.supplyAsync(() -> {
            Patient existingPatient = patientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));

            updatePatientFields(existingPatient, patientDTO);

            Patient updatedPatient = patientRepository.save(existingPatient);
            log.debug("Patient {} updated successfully", id);

            return convertToDTO(updatedPatient);
        }, taskExecutor);
    }

    @Async("taskExecutor")
    public CompletableFuture<PatientStatistics> getPatientStatisticsAsync() {
        log.info("Async: Calculating patient statistics");

        return CompletableFuture.supplyAsync(() -> {
            List<Patient> allPatients = patientRepository.findAll();

            long totalPatients = allPatients.size();
            long activePatients = allPatients.parallelStream().filter(Patient::getActive).count();

            var bloodGroupCounts = allPatients.parallelStream().filter(p -> p.getBloodGroup() != null)
                    .collect(Collectors.groupingByConcurrent(p -> p.getBloodGroup().name(), Collectors.counting()));

            var genderCounts = allPatients.parallelStream()
                    .collect(Collectors.groupingByConcurrent(p -> p.getGender().name(), Collectors.counting()));

            return new PatientStatistics(totalPatients, activePatients, bloodGroupCounts, genderCounts);
        }, taskExecutor);
    }

    @Async("batchExecutor")
    public CompletableFuture<Void> batchProcessPatientsAsync(List<Long> patientIds,
            java.util.function.Consumer<Patient> operation) {

        log.info("Async: Batch processing {} patients", patientIds.size());

        return CompletableFuture.runAsync(() -> {
            FunctionalUtils.partitionList(patientIds, 100).parallelStream().forEach(batch -> {
                List<Patient> patients = patientRepository.findAllById(batch);
                patients.forEach(operation);
                patientRepository.saveAll(patients);
            });

            log.info("Batch processing completed for {} patients", patientIds.size());
        }, taskExecutor);
    }

    private void updatePatientFields(Patient existingPatient, PatientDTO patientDTO) {
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

    public record PatientStatistics(long totalPatients, long activePatients,
            java.util.Map<String, Long> bloodGroupDistribution, java.util.Map<String, Long> genderDistribution) {
    }
}
