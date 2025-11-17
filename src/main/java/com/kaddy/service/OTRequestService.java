package com.kaddy.service;

import com.kaddy.dto.OTRequestDTO;
import com.kaddy.exception.DuplicateResourceException;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Doctor;
import com.kaddy.model.OTRequest;
import com.kaddy.model.Patient;
import com.kaddy.model.User;
import com.kaddy.model.enums.OTRequestStatus;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.OTRequestRepository;
import com.kaddy.repository.PatientRepository;
import com.kaddy.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OTRequestService {

    private final OTRequestRepository otRequestRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;
    private final SecurityUtils securityUtils;

    @Transactional
    public OTRequestDTO createOTRequest(OTRequestDTO otRequestDTO) {
        Patient patient = patientRepository.findById(otRequestDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + otRequestDTO.getPatientId()));

        Doctor surgeon = doctorRepository.findById(otRequestDTO.getSurgeonId())
                .orElseThrow(() -> new ResourceNotFoundException("Surgeon not found with id: " + otRequestDTO.getSurgeonId()));

        // Check for scheduling conflicts if room number and time are provided
        if (otRequestDTO.getOtRoomNumber() != null &&
            otRequestDTO.getScheduledStartTime() != null &&
            otRequestDTO.getScheduledEndTime() != null) {

            List<OTRequest> conflicts = otRequestRepository.findConflictingRequests(
                otRequestDTO.getOtRoomNumber(),
                otRequestDTO.getScheduledStartTime(),
                otRequestDTO.getScheduledEndTime()
            );

            if (!conflicts.isEmpty()) {
                throw new DuplicateResourceException("OT Room " + otRequestDTO.getOtRoomNumber() +
                    " is already booked for the requested time slot");
            }
        }

        OTRequest otRequest = new OTRequest();
        otRequest.setPatient(patient);
        otRequest.setSurgeon(surgeon);
        otRequest.setSurgeryType(otRequestDTO.getSurgeryType());
        otRequest.setSurgeryPurpose(otRequestDTO.getSurgeryPurpose());
        otRequest.setScheduledStartTime(otRequestDTO.getScheduledStartTime());
        otRequest.setScheduledEndTime(otRequestDTO.getScheduledEndTime());
        otRequest.setEstimatedDurationMinutes(otRequestDTO.getEstimatedDurationMinutes());
        otRequest.setOtRoomNumber(otRequestDTO.getOtRoomNumber());
        otRequest.setRequiredInstruments(otRequestDTO.getRequiredInstruments());
        otRequest.setRequiredMedications(otRequestDTO.getRequiredMedications());
        otRequest.setIsEmergency(otRequestDTO.getIsEmergency() != null ? otRequestDTO.getIsEmergency() : false);
        otRequest.setStatus(OTRequestStatus.PENDING);
        otRequest.setSurgeryNotes(otRequestDTO.getNotes());

        OTRequest saved = otRequestRepository.save(otRequest);
        return mapToDTO(saved);
    }

    public List<OTRequestDTO> getAllOTRequests() {
        return otRequestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public OTRequestDTO getOTRequestById(Long id) {
        OTRequest otRequest = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Request not found with id: " + id));
        return mapToDTO(otRequest);
    }

    public List<OTRequestDTO> getOTRequestsBySurgeon(Long surgeonId) {
        return otRequestRepository.findBySurgeonId(surgeonId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<OTRequestDTO> getOTRequestsByPatient(Long patientId) {
        return otRequestRepository.findByPatientId(patientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<OTRequestDTO> getOTRequestsByStatus(OTRequestStatus status) {
        return otRequestRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<OTRequestDTO> getPendingOTRequests() {
        return getOTRequestsByStatus(OTRequestStatus.PENDING);
    }

    public List<OTRequestDTO> getEmergencyPendingRequests() {
        return otRequestRepository.findEmergencyPendingRequests().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<OTRequestDTO> getOTRequestsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return otRequestRepository.findByScheduledStartTimeBetween(startDate, endDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OTRequestDTO updateOTRequest(Long id, OTRequestDTO otRequestDTO) {
        OTRequest existing = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Request not found with id: " + id));

        if (existing.getStatus() != OTRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot update OT request with status: " + existing.getStatus());
        }

        if (otRequestDTO.getSurgeryType() != null) {
            existing.setSurgeryType(otRequestDTO.getSurgeryType());
        }
        if (otRequestDTO.getSurgeryPurpose() != null) {
            existing.setSurgeryPurpose(otRequestDTO.getSurgeryPurpose());
        }
        if (otRequestDTO.getScheduledStartTime() != null) {
            existing.setScheduledStartTime(otRequestDTO.getScheduledStartTime());
        }
        if (otRequestDTO.getScheduledEndTime() != null) {
            existing.setScheduledEndTime(otRequestDTO.getScheduledEndTime());
        }
        if (otRequestDTO.getEstimatedDurationMinutes() != null) {
            existing.setEstimatedDurationMinutes(otRequestDTO.getEstimatedDurationMinutes());
        }
        if (otRequestDTO.getOtRoomNumber() != null) {
            existing.setOtRoomNumber(otRequestDTO.getOtRoomNumber());
        }
        if (otRequestDTO.getRequiredInstruments() != null) {
            existing.setRequiredInstruments(otRequestDTO.getRequiredInstruments());
        }
        if (otRequestDTO.getRequiredMedications() != null) {
            existing.setRequiredMedications(otRequestDTO.getRequiredMedications());
        }
        if (otRequestDTO.getNotes() != null) {
            existing.setSurgeryNotes(otRequestDTO.getNotes());
        }

        OTRequest updated = otRequestRepository.save(existing);
        return mapToDTO(updated);
    }

    @Transactional
    public OTRequestDTO approveOTRequest(Long id, String notes) {
        OTRequest otRequest = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Request not found with id: " + id));

        if (otRequest.getStatus() != OTRequestStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING requests");
        }

        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));

        otRequest.setStatus(OTRequestStatus.APPROVED);
        otRequest.setApprovedAt(LocalDateTime.now());
        otRequest.setApprovedBy(currentUser);
        if (notes != null) {
            otRequest.setApprovalNotes(notes);
        }

        OTRequest updated = otRequestRepository.save(otRequest);
        return mapToDTO(updated);
    }

    @Transactional
    public OTRequestDTO rejectOTRequest(Long id, String rejectionReason) {
        OTRequest otRequest = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Request not found with id: " + id));

        if (otRequest.getStatus() != OTRequestStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING requests");
        }

        otRequest.setStatus(OTRequestStatus.REJECTED);
        otRequest.setRejectionReason(rejectionReason);

        OTRequest updated = otRequestRepository.save(otRequest);
        return mapToDTO(updated);
    }

    @Transactional
    public OTRequestDTO startSurgery(Long id) {
        OTRequest otRequest = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Request not found with id: " + id));

        if (otRequest.getStatus() != OTRequestStatus.APPROVED) {
            throw new IllegalStateException("Can only start APPROVED surgeries");
        }

        otRequest.setStatus(OTRequestStatus.IN_PROGRESS);
        otRequest.setActualStartTime(LocalDateTime.now());

        OTRequest updated = otRequestRepository.save(otRequest);
        return mapToDTO(updated);
    }

    @Transactional
    public OTRequestDTO completeSurgery(Long id, String postOperativeNotes) {
        OTRequest otRequest = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Request not found with id: " + id));

        if (otRequest.getStatus() != OTRequestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only complete IN_PROGRESS surgeries");
        }

        otRequest.setStatus(OTRequestStatus.COMPLETED);
        otRequest.setActualEndTime(LocalDateTime.now());
        otRequest.setPostOperativeNotes(postOperativeNotes);

        OTRequest updated = otRequestRepository.save(otRequest);
        return mapToDTO(updated);
    }

    @Transactional
    public OTRequestDTO cancelOTRequest(Long id, String reason) {
        OTRequest otRequest = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Request not found with id: " + id));

        if (otRequest.getStatus() == OTRequestStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel COMPLETED surgery");
        }

        otRequest.setStatus(OTRequestStatus.CANCELLED);
        otRequest.setRejectionReason(reason);

        OTRequest updated = otRequestRepository.save(otRequest);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteOTRequest(Long id) {
        OTRequest otRequest = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT Request not found with id: " + id));

        if (Arrays.asList(OTRequestStatus.APPROVED, OTRequestStatus.IN_PROGRESS, OTRequestStatus.COMPLETED)
                .contains(otRequest.getStatus())) {
            throw new IllegalStateException("Cannot delete OT request with status: " + otRequest.getStatus());
        }

        otRequestRepository.delete(otRequest);
    }

    private OTRequestDTO mapToDTO(OTRequest otRequest) {
        OTRequestDTO dto = modelMapper.map(otRequest, OTRequestDTO.class);
        dto.setPatientId(otRequest.getPatient().getId());
        dto.setPatientName(otRequest.getPatient().getFirstName() + " " + otRequest.getPatient().getLastName());
        dto.setSurgeonId(otRequest.getSurgeon().getId());
        dto.setSurgeonName(otRequest.getSurgeon().getFirstName() + " " + otRequest.getSurgeon().getLastName());

        if (otRequest.getApprovedBy() != null) {
            dto.setApprovedById(otRequest.getApprovedBy().getId());
            dto.setApprovedByName(otRequest.getApprovedBy().getFirstName() + " " + otRequest.getApprovedBy().getLastName());
        }

        return dto;
    }
}
