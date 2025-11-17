package com.kaddy.service;

import com.kaddy.dto.LabTestDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Doctor;
import com.kaddy.model.LabTest;
import com.kaddy.model.Notification;
import com.kaddy.model.Patient;
import com.kaddy.model.User;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.LabTestRepository;
import com.kaddy.repository.PatientRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LabTestService {

    private final LabTestRepository labTestRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    public LabTestDTO getLabTestById(Long id) {
        log.info("Fetching lab test with ID: {}", id);
        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with ID: " + id));
        return convertToDTO(labTest);
    }

    public List<LabTestDTO> getAllLabTests() {
        log.info("Fetching all lab tests");
        return labTestRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LabTestDTO> getTestsByPatient(Long patientId) {
        log.info("Fetching lab tests for patient ID: {}", patientId);
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return labTestRepository.findByPatientIdOrderByOrderedDateDesc(patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LabTestDTO> getTestsByDoctor(Long doctorId) {
        log.info("Fetching lab tests for doctor ID: {}", doctorId);
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return labTestRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<LabTestDTO> getTestsByStatus(LabTest.TestStatus status) {
        log.info("Fetching lab tests with status: {}", status);
        return labTestRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LabTestDTO orderTest(LabTestDTO labTestDTO) {
        log.info("Ordering lab test: {} for patient ID: {}",
                labTestDTO.getTestName(), labTestDTO.getPatientId());

        Patient patient = patientRepository.findById(labTestDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + labTestDTO.getPatientId()));

        Doctor doctor = doctorRepository.findById(labTestDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + labTestDTO.getDoctorId()));

        LabTest labTest = convertToEntity(labTestDTO);
        labTest.setPatient(patient);
        labTest.setDoctor(doctor);
        labTest.setOrderedDate(LocalDateTime.now());

        if (labTest.getStatus() == null) {
            labTest.setStatus(LabTest.TestStatus.ORDERED);
        }

        if (labTest.getUrgent() == null) {
            labTest.setUrgent(false);
        }

        if (labTest.getPriority() == null) {
            labTest.setPriority(LabTest.TestPriority.NORMAL);
        }

        LabTest savedLabTest = labTestRepository.save(labTest);
        log.info("Lab test ordered successfully with ID: {}", savedLabTest.getId());

        return convertToDTO(savedLabTest);
    }

    public LabTestDTO updateLabTest(Long id, LabTestDTO labTestDTO) {
        log.info("Updating lab test with ID: {}", id);

        LabTest existingLabTest = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with ID: " + id));

        // Update fields
        existingLabTest.setTestName(labTestDTO.getTestName());
        existingLabTest.setTestType(labTestDTO.getTestType());
        existingLabTest.setSampleCollectedDate(labTestDTO.getSampleCollectedDate());
        existingLabTest.setResultDate(labTestDTO.getResultDate());
        existingLabTest.setTestResults(labTestDTO.getTestResults());
        existingLabTest.setResultFilePath(labTestDTO.getResultFilePath());
        existingLabTest.setNormalRange(labTestDTO.getNormalRange());
        existingLabTest.setUnit(labTestDTO.getUnit());
        existingLabTest.setRemarks(labTestDTO.getRemarks());
        existingLabTest.setStatus(labTestDTO.getStatus());
        existingLabTest.setPriority(labTestDTO.getPriority());
        existingLabTest.setUrgent(labTestDTO.getUrgent());

        // Update lab technician if provided
        if (labTestDTO.getLabTechnicianId() != null) {
            User labTechnician = userRepository.findById(labTestDTO.getLabTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lab technician not found with ID: " + labTestDTO.getLabTechnicianId()));
            existingLabTest.setLabTechnician(labTechnician);
        }

        LabTest updatedLabTest = labTestRepository.save(existingLabTest);
        log.info("Lab test updated successfully with ID: {}", updatedLabTest.getId());

        return convertToDTO(updatedLabTest);
    }

    public LabTestDTO uploadResults(Long id, String testResults, String resultFilePath, String remarks) {
        log.info("Uploading results for lab test with ID: {}", id);

        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with ID: " + id));

        labTest.setTestResults(testResults);
        labTest.setResultFilePath(resultFilePath);
        labTest.setRemarks(remarks);
        labTest.setResultDate(LocalDateTime.now());
        labTest.setStatus(LabTest.TestStatus.COMPLETED);

        LabTest updatedLabTest = labTestRepository.save(labTest);
        log.info("Lab test results uploaded successfully with ID: {}", updatedLabTest.getId());

        // Create notifications for patient and doctor
        createResultNotifications(updatedLabTest);

        return convertToDTO(updatedLabTest);
    }

    private void createResultNotifications(LabTest labTest) {
        log.info("Creating notifications for lab test results with ID: {}", labTest.getId());

        // Find user ID associated with the patient (if exists)
        // Note: In a real scenario, you might need to link Patient to User
        // For now, we'll create notifications if the patient/doctor has a User account

        // Notification for the doctor
        try {
            User doctorUser = userRepository.findByEmail(labTest.getDoctor().getEmail())
                    .orElse(null);

            if (doctorUser != null) {
                notificationService.createNotification(
                        doctorUser.getId(),
                        "Lab Test Results Available",
                        String.format("Lab test results for %s (Patient: %s) are now available.",
                                labTest.getTestName(),
                                labTest.getPatient().getFullName()),
                        Notification.NotificationType.LAB_RESULT_AVAILABLE,
                        "LAB_TEST",
                        labTest.getId()
                );
                log.info("Notification created for doctor ID: {}", doctorUser.getId());
            }
        } catch (Exception e) {
            log.warn("Could not create notification for doctor: {}", e.getMessage());
        }

        // Notification for the patient (if patient has a user account)
        try {
            User patientUser = userRepository.findByEmail(labTest.getPatient().getEmail())
                    .orElse(null);

            if (patientUser != null) {
                notificationService.createNotification(
                        patientUser.getId(),
                        "Your Lab Test Results Are Ready",
                        String.format("Your %s test results are now available. Please consult with your doctor.",
                                labTest.getTestName()),
                        Notification.NotificationType.LAB_RESULT_AVAILABLE,
                        "LAB_TEST",
                        labTest.getId()
                );
                log.info("Notification created for patient user ID: {}", patientUser.getId());
            }
        } catch (Exception e) {
            log.warn("Could not create notification for patient: {}", e.getMessage());
        }
    }

    public void deleteLabTest(Long id) {
        log.info("Deleting lab test with ID: {}", id);

        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with ID: " + id));

        labTest.setStatus(LabTest.TestStatus.CANCELLED);
        labTestRepository.save(labTest);
        log.info("Lab test cancelled successfully with ID: {}", id);
    }

    private LabTestDTO convertToDTO(LabTest labTest) {
        LabTestDTO dto = modelMapper.map(labTest, LabTestDTO.class);
        dto.setPatientId(labTest.getPatient().getId());
        dto.setDoctorId(labTest.getDoctor().getId());
        dto.setPatientName(labTest.getPatient().getFullName());
        dto.setDoctorName(labTest.getDoctor().getFullName());

        if (labTest.getLabTechnician() != null) {
            dto.setLabTechnicianId(labTest.getLabTechnician().getId());
            dto.setLabTechnicianName(labTest.getLabTechnician().getFirstName() + " " +
                    labTest.getLabTechnician().getLastName());
        }

        return dto;
    }

    private LabTest convertToEntity(LabTestDTO dto) {
        return modelMapper.map(dto, LabTest.class);
    }
}
