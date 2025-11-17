package com.kaddy.service;

import com.kaddy.dto.FollowUpDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Doctor;
import com.kaddy.model.FollowUp;
import com.kaddy.model.Patient;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.FollowUpRepository;
import com.kaddy.repository.PatientRepository;
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
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;

    public FollowUpDTO getFollowUpById(Long id) {
        log.info("Fetching follow-up with ID: {}", id);
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + id));
        return convertToDTO(followUp);
    }

    public List<FollowUpDTO> getAllFollowUps() {
        log.info("Fetching all follow-ups");
        return followUpRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FollowUpDTO> getFollowUpsByPatient(Long patientId) {
        log.info("Fetching follow-ups for patient ID: {}", patientId);
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return followUpRepository.findByPatientId(patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FollowUpDTO> getFollowUpsByDoctor(Long doctorId) {
        log.info("Fetching follow-ups for doctor ID: {}", doctorId);
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return followUpRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FollowUpDTO> getFollowUpsByStatus(FollowUp.FollowUpStatus status) {
        log.info("Fetching follow-ups with status: {}", status);
        return followUpRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FollowUpDTO> getFollowUpsByPatientAndStatus(Long patientId, FollowUp.FollowUpStatus status) {
        log.info("Fetching follow-ups for patient ID: {} with status: {}", patientId, status);
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return followUpRepository.findByPatientIdAndStatusOrderByFollowupDateDesc(patientId, status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public FollowUpDTO scheduleFollowUp(FollowUpDTO followUpDTO) {
        log.info("Scheduling follow-up for patient ID: {} with doctor ID: {}",
                followUpDTO.getPatientId(), followUpDTO.getDoctorId());

        Patient patient = patientRepository.findById(followUpDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + followUpDTO.getPatientId()));

        Doctor doctor = doctorRepository.findById(followUpDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + followUpDTO.getDoctorId()));

        FollowUp followUp = convertToEntity(followUpDTO);
        followUp.setPatient(patient);
        followUp.setDoctor(doctor);
        followUp.setScheduledDate(LocalDateTime.now());

        if (followUp.getStatus() == null) {
            followUp.setStatus(FollowUp.FollowUpStatus.SCHEDULED);
        }

        FollowUp savedFollowUp = followUpRepository.save(followUp);
        log.info("Follow-up scheduled successfully with ID: {}", savedFollowUp.getId());

        return convertToDTO(savedFollowUp);
    }

    public FollowUpDTO updateFollowUp(Long id, FollowUpDTO followUpDTO) {
        log.info("Updating follow-up with ID: {}", id);

        FollowUp existingFollowUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + id));

        // Update fields
        existingFollowUp.setFollowupDate(followUpDTO.getFollowupDate());
        existingFollowUp.setReason(followUpDTO.getReason());
        existingFollowUp.setDiagnosis(followUpDTO.getDiagnosis());
        existingFollowUp.setPrescription(followUpDTO.getPrescription());
        existingFollowUp.setNotes(followUpDTO.getNotes());
        existingFollowUp.setVitalSigns(followUpDTO.getVitalSigns());
        existingFollowUp.setTreatmentPlan(followUpDTO.getTreatmentPlan());
        existingFollowUp.setNextFollowupDate(followUpDTO.getNextFollowupDate());
        existingFollowUp.setStatus(followUpDTO.getStatus());
        existingFollowUp.setDurationMinutes(followUpDTO.getDurationMinutes());

        // Update patient and doctor if needed
        if (followUpDTO.getPatientId() != null && !followUpDTO.getPatientId().equals(existingFollowUp.getPatient().getId())) {
            Patient patient = patientRepository.findById(followUpDTO.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + followUpDTO.getPatientId()));
            existingFollowUp.setPatient(patient);
        }

        if (followUpDTO.getDoctorId() != null && !followUpDTO.getDoctorId().equals(existingFollowUp.getDoctor().getId())) {
            Doctor doctor = doctorRepository.findById(followUpDTO.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + followUpDTO.getDoctorId()));
            existingFollowUp.setDoctor(doctor);
        }

        FollowUp updatedFollowUp = followUpRepository.save(existingFollowUp);
        log.info("Follow-up updated successfully with ID: {}", updatedFollowUp.getId());

        return convertToDTO(updatedFollowUp);
    }

    public FollowUpDTO completeFollowUp(Long id) {
        log.info("Completing follow-up with ID: {}", id);

        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + id));

        followUp.setStatus(FollowUp.FollowUpStatus.COMPLETED);
        followUp.setFollowupDate(LocalDateTime.now());

        FollowUp updatedFollowUp = followUpRepository.save(followUp);
        log.info("Follow-up completed successfully with ID: {}", updatedFollowUp.getId());

        return convertToDTO(updatedFollowUp);
    }

    public void deleteFollowUp(Long id) {
        log.info("Deleting follow-up with ID: {}", id);

        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + id));

        followUp.setStatus(FollowUp.FollowUpStatus.CANCELLED);
        followUpRepository.save(followUp);
        log.info("Follow-up cancelled successfully with ID: {}", id);
    }

    private FollowUpDTO convertToDTO(FollowUp followUp) {
        FollowUpDTO dto = modelMapper.map(followUp, FollowUpDTO.class);
        dto.setPatientId(followUp.getPatient().getId());
        dto.setDoctorId(followUp.getDoctor().getId());
        dto.setPatientName(followUp.getPatient().getFullName());
        dto.setDoctorName(followUp.getDoctor().getFullName());
        return dto;
    }

    private FollowUp convertToEntity(FollowUpDTO dto) {
        return modelMapper.map(dto, FollowUp.class);
    }
}
