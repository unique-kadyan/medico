package com.kaddy.service;

import com.kaddy.dto.NursePatientAssignmentDTO;
import com.kaddy.exception.DuplicateResourceException;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.NursePatientAssignment;
import com.kaddy.model.Patient;
import com.kaddy.model.User;
import com.kaddy.model.enums.UserRole;
import com.kaddy.repository.NursePatientAssignmentRepository;
import com.kaddy.repository.PatientRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NursePatientAssignmentService {

    private final NursePatientAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public NursePatientAssignmentDTO assignNurseToPatient(NursePatientAssignmentDTO dto) {
        log.info("Assigning nurse {} to patient {}", dto.getNurseId(), dto.getPatientId());

        User nurse = userRepository.findById(dto.getNurseId())
                .orElseThrow(() -> new ResourceNotFoundException("Nurse not found with id: " + dto.getNurseId()));

        if (nurse.getRole() != UserRole.NURSE) {
            throw new IllegalArgumentException("User with id " + dto.getNurseId() + " is not a nurse");
        }

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        List<NursePatientAssignment> existingAssignments = assignmentRepository
                .findByNurseIdAndPatientId(dto.getNurseId(), dto.getPatientId());

        if (!existingAssignments.isEmpty()) {
            boolean hasActiveAssignment = existingAssignments.stream().anyMatch(NursePatientAssignment::isActive);
            if (hasActiveAssignment) {
                throw new DuplicateResourceException("Active assignment already exists between nurse "
                        + dto.getNurseId() + " and patient " + dto.getPatientId());
            }
        }

        NursePatientAssignment assignment = new NursePatientAssignment();
        assignment.setNurse(nurse);
        assignment.setPatient(patient);
        assignment.setAssignedAs(dto.getAssignedAs() != null ? dto.getAssignedAs() : "PRIMARY");
        assignment.setNotes(dto.getNotes());
        assignment.setActive(true);

        NursePatientAssignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Successfully assigned nurse {} to patient {}", nurse.getId(), patient.getId());

        return convertToDTO(savedAssignment);
    }

    @Transactional(readOnly = true)
    public List<NursePatientAssignmentDTO> getAssignmentsByNurse(Long nurseId) {
        log.info("Fetching assignments for nurse: {}", nurseId);
        List<NursePatientAssignment> assignments = assignmentRepository.findByNurseId(nurseId);
        return assignments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NursePatientAssignmentDTO> getActiveAssignmentsByNurse(Long nurseId) {
        log.info("Fetching active assignments for nurse: {}", nurseId);
        List<NursePatientAssignment> assignments = assignmentRepository.findByNurseIdAndActive(nurseId, true);
        return assignments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NursePatientAssignmentDTO> getAssignmentsByPatient(Long patientId) {
        log.info("Fetching assignments for patient: {}", patientId);
        List<NursePatientAssignment> assignments = assignmentRepository.findByPatientId(patientId);
        return assignments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NursePatientAssignmentDTO> getActiveAssignmentsByPatient(Long patientId) {
        log.info("Fetching active assignments for patient: {}", patientId);
        List<NursePatientAssignment> assignments = assignmentRepository.findByPatientIdAndActive(patientId, true);
        return assignments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NursePatientAssignmentDTO> getAllAssignments() {
        log.info("Fetching all nurse-patient assignments");
        List<NursePatientAssignment> assignments = assignmentRepository.findAll();
        return assignments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NursePatientAssignmentDTO getAssignmentById(Long id) {
        log.info("Fetching assignment with id: {}", id);
        NursePatientAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        return convertToDTO(assignment);
    }

    public void deactivateAssignment(Long id) {
        log.info("Deactivating assignment with id: {}", id);
        NursePatientAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        assignment.setActive(false);
        assignmentRepository.save(assignment);
        log.info("Successfully deactivated assignment {}", id);
    }

    public void deleteAssignment(Long id) {
        log.info("Deleting assignment with id: {}", id);
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + id);
        }
        assignmentRepository.deleteById(id);
        log.info("Successfully deleted assignment {}", id);
    }

    @Transactional(readOnly = true)
    public long getActivePatientCountForNurse(Long nurseId) {
        log.info("Getting active patient count for nurse: {}", nurseId);
        return assignmentRepository.countActiveAssignmentsByNurse(nurseId);
    }

    private NursePatientAssignmentDTO convertToDTO(NursePatientAssignment assignment) {
        return NursePatientAssignmentDTO.builder().id(assignment.getId()).nurseId(assignment.getNurse().getId())
                .nurseFirstName(assignment.getNurse().getFirstName()).nurseLastName(assignment.getNurse().getLastName())
                .nurseEmail(assignment.getNurse().getEmail()).patientId(assignment.getPatient().getId())
                .patientFirstName(assignment.getPatient().getFirstName())
                .patientLastName(assignment.getPatient().getLastName())
                .patientIdentifier(assignment.getPatient().getPatientId()).assignedAs(assignment.getAssignedAs())
                .notes(assignment.getNotes()).active(assignment.isActive()).createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt()).build();
    }
}
