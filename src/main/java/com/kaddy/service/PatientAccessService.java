package com.kaddy.service;

import com.kaddy.model.DoctorPatientAssignment;
import com.kaddy.model.Patient;
import com.kaddy.model.User;
import com.kaddy.model.enums.UserRole;
import com.kaddy.repository.DoctorPatientAssignmentRepository;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.PatientRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientAccessService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorPatientAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<Patient> getAccessiblePatients() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN ||
                currentUser.getRole() == UserRole.RECEPTIONIST ||
                currentUser.getRole() == UserRole.PHARMACIST) {
            return patientRepository.findAll();
        }

        if (currentUser.getRole() == UserRole.DOCTOR || currentUser.getRole() == UserRole.NURSE) {
            return getAssignedPatients(currentUser);
        }

        log.warn("User {} with role {} attempted to access patient list", currentUser.getUsername(),
                currentUser.getRole());
        return List.of();
    }

    @Transactional(readOnly = true)
    public boolean canAccessPatient(Long patientId) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN ||
                currentUser.getRole() == UserRole.RECEPTIONIST ||
                currentUser.getRole() == UserRole.PHARMACIST) {
            return true;
        }

        if (currentUser.getRole() == UserRole.DOCTOR || currentUser.getRole() == UserRole.NURSE) {
            return isPatientAssignedToUser(patientId, currentUser);
        }

        return false;
    }

    @Transactional(readOnly = true)
    public Patient getAccessiblePatient(Long patientId) {
        if (!canAccessPatient(patientId)) {
            throw new RuntimeException("Access denied: You do not have permission to view this patient");
        }

        return patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
    }

    private List<Patient> getAssignedPatients(User user) {
        var doctorOpt = doctorRepository.findAll().stream().filter(d -> {
            if (d.getUser() != null && d.getUser().getId().equals(user.getId())) {
                return true;
            }
            if (d.getEmail() != null && d.getEmail().equalsIgnoreCase(user.getEmail())) {
                return true;
            }
            return false;
        }).findFirst();

        if (doctorOpt.isEmpty()) {
            log.warn("No doctor profile found for user: {}", user.getUsername());
            return List.of();
        }

        Long doctorId = doctorOpt.get().getId();

        List<DoctorPatientAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctorId))
                .filter(a -> a.getActive() != null && a.getActive()).collect(Collectors.toList());

        return assignments.stream().map(DoctorPatientAssignment::getPatient).filter(p -> p != null)
                .collect(Collectors.toList());
    }

    private boolean isPatientAssignedToUser(Long patientId, User user) {
        List<Patient> assignedPatients = getAssignedPatients(user);
        return assignedPatients.stream().anyMatch(p -> p.getId().equals(patientId));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username).or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
