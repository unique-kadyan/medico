package com.kaddy.service;

import com.kaddy.dto.MedicationDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Medication;
import com.kaddy.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "medications", key = "#id")
    public MedicationDTO getMedicationById(Long id) {
        log.info("Fetching medication with ID: {}", id);
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with ID: " + id));
        return convertToDTO(medication);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "medications", key = "#medicationCode")
    public MedicationDTO getMedicationByCode(String medicationCode) {
        log.info("Fetching medication with code: {}", medicationCode);
        Medication medication = medicationRepository.findByMedicationCode(medicationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with code: " + medicationCode));
        return convertToDTO(medication);
    }

    @Transactional(readOnly = true)
    public List<MedicationDTO> getAllMedications() {
        log.info("Fetching all medications");
        return medicationRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationDTO> getAllActiveMedications() {
        log.info("Fetching all active medications");
        return medicationRepository.findAllActiveMedications().stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationDTO> searchMedicationsByName(String name) {
        log.info("Searching medications by name: {}", name);
        return medicationRepository.findByNameContainingIgnoreCase(name).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationDTO> getMedicationsByCategory(String category) {
        log.info("Fetching medications by category: {}", category);
        return medicationRepository.findByCategory(category).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationDTO> getLowStockMedications() {
        log.info("Fetching low stock medications");
        return medicationRepository.findLowStockMedications().stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationDTO> getExpiredMedications() {
        log.info("Fetching expired medications");
        return medicationRepository.findExpiredMedications(LocalDate.now()).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationDTO> getExpiringSoonMedications() {
        log.info("Fetching medications expiring within 3 months");
        LocalDate threeMonthsLater = LocalDate.now().plusMonths(3);
        return medicationRepository.findMedicationsExpiringBetween(LocalDate.now(), threeMonthsLater).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @CacheEvict(value = "medications", allEntries = true)
    public MedicationDTO createMedication(MedicationDTO medicationDTO) {
        log.info("Creating new medication with code: {}", medicationDTO.getMedicationCode());

        if (medicationRepository.existsByMedicationCode(medicationDTO.getMedicationCode())) {
            throw new IllegalArgumentException(
                    "Medication with code " + medicationDTO.getMedicationCode() + " already exists");
        }

        Medication medication = convertToEntity(medicationDTO);
        Medication savedMedication = medicationRepository.save(medication);
        log.info("Medication created successfully with ID: {}", savedMedication.getId());

        return convertToDTO(savedMedication);
    }

    @CacheEvict(value = "medications", allEntries = true)
    public MedicationDTO updateMedication(Long id, MedicationDTO medicationDTO) {
        log.info("Updating medication with ID: {}", id);

        Medication existingMedication = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with ID: " + id));

        existingMedication.setName(medicationDTO.getName());
        existingMedication.setGenericName(medicationDTO.getGenericName());
        existingMedication.setCategory(medicationDTO.getCategory());
        existingMedication.setManufacturer(medicationDTO.getManufacturer());
        existingMedication.setDescription(medicationDTO.getDescription());
        existingMedication.setDosageForm(medicationDTO.getDosageForm());
        existingMedication.setStrength(medicationDTO.getStrength());
        existingMedication.setUnitPrice(medicationDTO.getUnitPrice());
        existingMedication.setStockQuantity(medicationDTO.getStockQuantity());
        existingMedication.setReorderLevel(medicationDTO.getReorderLevel());
        existingMedication.setReorderQuantity(medicationDTO.getReorderQuantity());
        existingMedication.setExpiryDate(medicationDTO.getExpiryDate());
        existingMedication.setBatchNumber(medicationDTO.getBatchNumber());
        existingMedication.setRequiresPrescription(medicationDTO.getRequiresPrescription());
        existingMedication.setSideEffects(medicationDTO.getSideEffects());
        existingMedication.setContraindications(medicationDTO.getContraindications());
        existingMedication.setStorageInstructions(medicationDTO.getStorageInstructions());

        Medication updatedMedication = medicationRepository.save(existingMedication);
        log.info("Medication updated successfully with ID: {}", updatedMedication.getId());

        return convertToDTO(updatedMedication);
    }

    @CacheEvict(value = "medications", allEntries = true)
    public MedicationDTO updateStock(Long id, Integer quantity) {
        log.info("Updating stock for medication ID: {} with quantity: {}", id, quantity);

        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with ID: " + id));

        medication.setStockQuantity(medication.getStockQuantity() + quantity);
        Medication updatedMedication = medicationRepository.save(medication);

        log.info("Stock updated successfully for medication ID: {}", id);
        return convertToDTO(updatedMedication);
    }

    @CacheEvict(value = "medications", allEntries = true)
    public void deleteMedication(Long id) {
        log.info("Deleting medication with ID: {}", id);

        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with ID: " + id));

        medication.setActive(false);
        medicationRepository.save(medication);
        log.info("Medication deactivated successfully with ID: {}", id);
    }

    private MedicationDTO convertToDTO(Medication medication) {
        MedicationDTO dto = modelMapper.map(medication, MedicationDTO.class);
        dto.setLowStock(medication.isLowStock());
        dto.setExpired(medication.isExpired());
        dto.setExpiringSoon(medication.isExpiringSoon());
        return dto;
    }

    private Medication convertToEntity(MedicationDTO dto) {
        return modelMapper.map(dto, Medication.class);
    }
}
