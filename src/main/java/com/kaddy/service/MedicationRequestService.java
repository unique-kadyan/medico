package com.kaddy.service;

import com.kaddy.dto.MedicationRequestDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Medication;
import com.kaddy.model.MedicationRequest;
import com.kaddy.model.Notification;
import com.kaddy.model.User;
import com.kaddy.model.enums.MedicationRequestStatus;
import com.kaddy.repository.MedicationRepository;
import com.kaddy.repository.MedicationRequestRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicationRequestService {

    private final MedicationRequestRepository medicationRequestRepository;
    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    public MedicationRequestDTO createRequest(MedicationRequestDTO requestDTO) {
        log.info("Creating medication request for medication: {}", requestDTO.getMedicationName());

        User requestedBy = userRepository.findById(requestDTO.getRequestedById()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getRequestedById()));

        MedicationRequest request = convertToEntity(requestDTO);
        request.setRequestedBy(requestedBy);
        request.setStatus(MedicationRequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());

        MedicationRequest savedRequest = medicationRequestRepository.save(request);
        log.info("Medication request created successfully with ID: {}", savedRequest.getId());

        return convertToDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<MedicationRequestDTO> getAllRequests() {
        log.info("Fetching all medication requests");
        return medicationRequestRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicationRequestDTO getRequestById(Long id) {
        log.info("Fetching medication request with ID: {}", id);
        MedicationRequest request = medicationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication request not found with ID: " + id));
        return convertToDTO(request);
    }

    @Transactional(readOnly = true)
    public List<MedicationRequestDTO> getRequestsByStatus(MedicationRequestStatus status) {
        log.info("Fetching medication requests with status: {}", status);
        return medicationRequestRepository.findByStatusOrderByRequestDateDesc(status).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationRequestDTO> getRequestsByDoctor(Long doctorId) {
        log.info("Fetching medication requests by doctor ID: {}", doctorId);

        if (!userRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("User not found with ID: " + doctorId);
        }

        return medicationRequestRepository.findByRequestedById(doctorId).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MedicationRequestDTO approveRequest(Long id, Long reviewerId, String reviewNotes) {
        log.info("Approving medication request with ID: {} by reviewer ID: {}", id, reviewerId);

        MedicationRequest request = medicationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication request not found with ID: " + id));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with ID: " + reviewerId));

        if (request.getStatus() != MedicationRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved");
        }

        request.setStatus(MedicationRequestStatus.APPROVED);
        request.setReviewedBy(reviewer);
        request.setReviewDate(LocalDateTime.now());
        request.setReviewNotes(reviewNotes);

        MedicationRequest updatedRequest = medicationRequestRepository.save(request);
        log.info("Medication request approved successfully with ID: {}", updatedRequest.getId());

        Medication newMedication = createMedicationFromRequest(request);
        medicationRepository.save(newMedication);
        log.info("New medication created from request: {} with code: {}", newMedication.getName(),
                newMedication.getMedicationCode());

        String notificationTitle = "Medication Request Approved";
        String notificationMessage = String.format(
                "Your medication request for '%s' has been approved by %s %s and added to the inventory. Review notes: %s",
                request.getMedicationName(), reviewer.getFirstName(), reviewer.getLastName(),
                reviewNotes != null ? reviewNotes : "None");

        notificationService.createNotification(request.getRequestedBy().getId(), notificationTitle, notificationMessage,
                Notification.NotificationType.INFO, "MEDICATION_REQUEST", request.getId());

        return convertToDTO(updatedRequest);
    }

    private Medication createMedicationFromRequest(MedicationRequest request) {
        Medication medication = new Medication();

        String baseCode = request.getMedicationName().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (baseCode.length() > 6) {
            baseCode = baseCode.substring(0, 6);
        }
        String uniqueCode = baseCode + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        medication.setMedicationCode(uniqueCode);

        medication.setName(request.getMedicationName());
        medication.setDescription(request.getDescription());
        medication.setManufacturer(request.getManufacturer());
        medication.setDosageForm(request.getDosageForm() != null ? request.getDosageForm().toUpperCase() : "TABLET");
        medication.setStrength(request.getStrength());
        medication.setCategory("OTHER");
        medication.setUnitPrice(request.getEstimatedCost() != null ? request.getEstimatedCost() : BigDecimal.ZERO);
        medication.setStockQuantity(0);
        medication.setReorderLevel(10);
        medication.setReorderQuantity(50);
        medication.setRequiresPrescription(true);

        return medication;
    }

    public MedicationRequestDTO rejectRequest(Long id, Long reviewerId, String reviewNotes) {
        log.info("Rejecting medication request with ID: {} by reviewer ID: {}", id, reviewerId);

        MedicationRequest request = medicationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication request not found with ID: " + id));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with ID: " + reviewerId));

        if (request.getStatus() != MedicationRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }

        request.setStatus(MedicationRequestStatus.REJECTED);
        request.setReviewedBy(reviewer);
        request.setReviewDate(LocalDateTime.now());
        request.setReviewNotes(reviewNotes);

        MedicationRequest updatedRequest = medicationRequestRepository.save(request);
        log.info("Medication request rejected successfully with ID: {}", updatedRequest.getId());

        String notificationTitle = "Medication Request Rejected";
        String notificationMessage = String.format(
                "Your medication request for '%s' has been rejected by %s %s. Reason: %s", request.getMedicationName(),
                reviewer.getFirstName(), reviewer.getLastName(), reviewNotes != null ? reviewNotes : "Not specified");

        notificationService.createNotification(request.getRequestedBy().getId(), notificationTitle, notificationMessage,
                Notification.NotificationType.ALERT, "MEDICATION_REQUEST", request.getId());

        return convertToDTO(updatedRequest);
    }

    private MedicationRequestDTO convertToDTO(MedicationRequest request) {
        MedicationRequestDTO dto = modelMapper.map(request, MedicationRequestDTO.class);
        dto.setRequestedById(request.getRequestedBy().getId());
        dto.setRequestedByName(request.getRequestedBy().getFirstName() + " " + request.getRequestedBy().getLastName());

        if (request.getReviewedBy() != null) {
            dto.setReviewedById(request.getReviewedBy().getId());
            dto.setReviewedByName(request.getReviewedBy().getFirstName() + " " + request.getReviewedBy().getLastName());
        }

        return dto;
    }

    private MedicationRequest convertToEntity(MedicationRequestDTO dto) {
        return modelMapper.map(dto, MedicationRequest.class);
    }
}
