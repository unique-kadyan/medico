package com.kaddy.service;

import com.kaddy.dto.PrescriptionDTO;
import com.kaddy.dto.PrescriptionItemDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Prescription;
import com.kaddy.model.PrescriptionItem;
import com.kaddy.model.User;
import com.kaddy.repository.PrescriptionRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PrescriptionDTO getPrescriptionById(Long id) {
        log.info("Fetching prescription with ID: {}", id);
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));
        return convertToDTO(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionDTO getPrescriptionByNumber(String prescriptionNumber) {
        log.info("Fetching prescription with number: {}", prescriptionNumber);
        Prescription prescription = prescriptionRepository.findByPrescriptionNumber(prescriptionNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with number: " + prescriptionNumber));
        return convertToDTO(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getAllPrescriptions() {
        log.info("Fetching all prescriptions");
        return prescriptionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPrescriptionsByPatientId(Long patientId) {
        log.info("Fetching prescriptions for patient ID: {}", patientId);
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getUndispensedPrescriptionsByPatientId(Long patientId) {
        log.info("Fetching undispensed prescriptions for patient ID: {}", patientId);
        return prescriptionRepository.findUndispensedPrescriptionsByPatient(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getUndispensedPrescriptions() {
        log.info("Fetching all undispensed prescriptions");
        return prescriptionRepository.findUndispensedPrescriptions().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPrescriptionsByDoctorId(Long doctorId) {
        log.info("Fetching prescriptions by doctor ID: {}", doctorId);
        return prescriptionRepository.findByDoctorId(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PrescriptionDTO dispensePrescription(Long prescriptionId, Long dispensedById) {
        log.info("Dispensing prescription ID: {} by user ID: {}", prescriptionId, dispensedById);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        if (prescription.getDispensed()) {
            throw new IllegalStateException("Prescription is already dispensed");
        }

        if (prescription.isExpired()) {
            throw new IllegalStateException("Prescription is expired");
        }

        User dispensedBy = userRepository.findById(dispensedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + dispensedById));

        prescription.setDispensed(true);
        prescription.setDispensedDate(LocalDate.now());
        prescription.setDispensedBy(dispensedBy);

        Prescription updatedPrescription = prescriptionRepository.save(prescription);
        log.info("Prescription {} dispensed successfully", prescriptionId);
        return convertToDTO(updatedPrescription);
    }

    private PrescriptionDTO convertToDTO(Prescription prescription) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(prescription.getId());
        dto.setPrescriptionNumber(prescription.getPrescriptionNumber());
        dto.setPatientId(prescription.getPatient().getId());
        dto.setPatientName(prescription.getPatient().getUser().getFirstName() + " " +
                prescription.getPatient().getUser().getLastName());
        dto.setDoctorId(prescription.getDoctor().getId());
        dto.setDoctorName(prescription.getDoctor().getUser().getFirstName() + " " +
                prescription.getDoctor().getUser().getLastName());
        dto.setPrescriptionDate(prescription.getPrescriptionDate());
        dto.setExpiryDate(prescription.getExpiryDate());
        dto.setDiagnosis(prescription.getDiagnosis());
        dto.setInstructions(prescription.getInstructions());
        dto.setDispensed(prescription.getDispensed());
        dto.setDispensedDate(prescription.getDispensedDate());
        dto.setExpired(prescription.isExpired());

        if (prescription.getDispensedBy() != null) {
            dto.setDispensedById(prescription.getDispensedBy().getId());
            dto.setDispensedByName(prescription.getDispensedBy().getFirstName() + " " +
                    prescription.getDispensedBy().getLastName());
        }

        List<PrescriptionItemDTO> itemDTOs = prescription.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    private PrescriptionItemDTO convertItemToDTO(PrescriptionItem item) {
        PrescriptionItemDTO dto = new PrescriptionItemDTO();
        dto.setId(item.getId());
        dto.setPrescriptionId(item.getPrescription().getId());
        dto.setMedicationId(item.getMedication().getId());
        dto.setMedicationName(item.getMedication().getName());
        dto.setMedicationCode(item.getMedication().getMedicationCode());
        dto.setQuantity(item.getQuantity());
        dto.setDosage(item.getDosage());
        dto.setFrequency(item.getFrequency());
        dto.setDuration(item.getDuration());
        dto.setInstructions(item.getInstructions());
        dto.setWarnings(item.getWarnings());
        return dto;
    }
}
