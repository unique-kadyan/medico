package com.kaddy.service;

import com.kaddy.dto.DoctorPatientAssignmentDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Doctor;
import com.kaddy.model.DoctorPatientAssignment;
import com.kaddy.model.Patient;
import com.kaddy.repository.DoctorPatientAssignmentRepository;
import com.kaddy.repository.DoctorRepository;
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
public class DoctorPatientAssignmentService {

    private final DoctorPatientAssignmentRepository assignmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;

    public DoctorPatientAssignmentDTO assignDoctorToPatient(DoctorPatientAssignmentDTO assignmentDTO) {
        log.info("Assigning doctor {} to patient {}", assignmentDTO.getDoctorId(), assignmentDTO.getPatientId());

        Doctor doctor = doctorRepository.findById(assignmentDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + assignmentDTO.getDoctorId()));

        Patient patient = patientRepository.findById(assignmentDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + assignmentDTO.getPatientId()));

        DoctorPatientAssignment assignment = new DoctorPatientAssignment();
        assignment.setDoctor(doctor);
        assignment.setPatient(patient);
        assignment.setAssignedDate(LocalDateTime.now());
        assignment.setPrimaryDoctor(assignmentDTO.getPrimaryDoctor());
        assignment.setNotes(assignmentDTO.getNotes());
        assignment.setStatus(DoctorPatientAssignment.AssignmentStatus.ACTIVE);

        DoctorPatientAssignment saved = assignmentRepository.save(assignment);
        return convertToDTO(saved);
    }

    public List<DoctorPatientAssignmentDTO> getDoctorAssignments(Long doctorId) {
        log.info("Getting assignments for doctor {}", doctorId);
        return assignmentRepository.findActiveAssignmentsByDoctorId(doctorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorPatientAssignmentDTO> getPatientAssignments(Long patientId) {
        log.info("Getting assignments for patient {}", patientId);
        return assignmentRepository.findActiveAssignmentsByPatientId(patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Long> getPatientIdsForDoctor(Long doctorId) {
        return assignmentRepository.findPatientIdsByDoctorId(doctorId);
    }

    public long getPatientCountForDoctor(Long doctorId) {
        log.info("Getting patient count for doctor {}", doctorId);
        return assignmentRepository.findPatientIdsByDoctorId(doctorId).size();
    }

    public void removeAssignment(Long assignmentId) {
        log.info("Removing assignment {}", assignmentId);
        DoctorPatientAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        assignment.setStatus(DoctorPatientAssignment.AssignmentStatus.COMPLETED);
        assignmentRepository.save(assignment);
    }

    private DoctorPatientAssignmentDTO convertToDTO(DoctorPatientAssignment assignment) {
        DoctorPatientAssignmentDTO dto = modelMapper.map(assignment, DoctorPatientAssignmentDTO.class);
        dto.setDoctorId(assignment.getDoctor().getId());
        dto.setPatientId(assignment.getPatient().getId());
        dto.setDoctorName(assignment.getDoctor().getFirstName() + " " + assignment.getDoctor().getLastName());
        dto.setPatientName(assignment.getPatient().getFirstName() + " " + assignment.getPatient().getLastName());
        return dto;
    }
}
