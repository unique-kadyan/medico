package com.kaddy.service;

import com.kaddy.dto.AppointmentDTO;
import com.kaddy.dto.AppointmentRequest;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Appointment;
import com.kaddy.model.Doctor;
import com.kaddy.model.Patient;
import com.kaddy.model.User;
import com.kaddy.model.enums.AppointmentStatus;
import com.kaddy.model.enums.UserRole;
import com.kaddy.repository.AppointmentRepository;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.PatientRepository;
import com.kaddy.repository.UserRepository;
import com.kaddy.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllAppointments() {
        log.info("Fetching all appointments");

        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Appointment> appointments;

        if (currentUser.getRole() == UserRole.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
            appointments = appointmentRepository.findByDoctorId(doctor.getId());
        } else if (currentUser.getRole() == UserRole.PATIENT) {
            Patient patient = patientRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            appointments = appointmentRepository.findByPatientId(patient.getId());
        } else if (currentUser.getRole() == UserRole.NURSE) {
            appointments = appointmentRepository.findAll();
        } else {
            appointments = appointmentRepository.findAll();
        }

        return appointments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id) {
        log.info("Fetching appointment with id: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return convertToDTO(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        log.info("Fetching appointments for patient: {}", patientId);
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId) {
        log.info("Fetching appointments for doctor: {}", doctorId);
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getTodaysAppointments() {
        log.info("Fetching today's appointments");
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository.findAppointmentsBetweenDates(startOfDay, endOfDay);

        return appointments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getUpcomingAppointments() {
        log.info("Fetching upcoming appointments");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusMonths(3);

        List<Appointment> appointments = appointmentRepository.findAppointmentsBetweenDates(now, futureDate);

        return appointments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public AppointmentDTO createAppointment(AppointmentRequest request) {
        log.info("Creating appointment for patient {} with doctor {}", request.getPatientId(), request.getDoctorId());

        Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(
                () -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        appointment.setStatus(request.getStatus() != null ? request.getStatus() : AppointmentStatus.SCHEDULED);
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setSymptoms(request.getSymptoms());
        appointment.setDiagnosis(request.getDiagnosis());
        appointment.setNotes(request.getNotes());
        appointment.setDuration(request.getDuration() != null ? request.getDuration() : 30);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created successfully with id: {}", savedAppointment.getId());

        return convertToDTO(savedAppointment);
    }

    public AppointmentDTO updateAppointment(Long id, AppointmentRequest request) {
        log.info("Updating appointment with id: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(
                    () -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
            appointment.setPatient(patient);
        }

        if (request.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(request.getDoctorId()).orElseThrow(
                    () -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));
            appointment.setDoctor(doctor);
        }

        if (request.getAppointmentDateTime() != null) {
            appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        }

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        if (request.getReasonForVisit() != null) {
            appointment.setReasonForVisit(request.getReasonForVisit());
        }

        if (request.getSymptoms() != null) {
            appointment.setSymptoms(request.getSymptoms());
        }

        if (request.getDiagnosis() != null) {
            appointment.setDiagnosis(request.getDiagnosis());
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        if (request.getDuration() != null) {
            appointment.setDuration(request.getDuration());
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment updated successfully");

        return convertToDTO(updatedAppointment);
    }

    public AppointmentDTO updateAppointmentStatus(Long id, AppointmentStatus status) {
        log.info("Updating appointment {} status to {}", id, status);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        appointment.setStatus(status);

        if (status == AppointmentStatus.COMPLETED && appointment.getActualStartTime() != null
                && appointment.getActualEndTime() == null) {
            appointment.setActualEndTime(LocalDateTime.now());
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment status updated successfully");

        return convertToDTO(updatedAppointment);
    }

    public void deleteAppointment(Long id) {
        log.info("Deleting appointment with id: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        appointmentRepository.delete(appointment);
        log.info("Appointment deleted successfully");
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        return AppointmentDTO.builder().id(appointment.getId()).patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(
                        "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName())
                .appointmentDate(appointment.getAppointmentDateTime())
                .appointmentDateTime(appointment.getAppointmentDateTime()).status(appointment.getStatus())
                .type(appointment.getReasonForVisit() != null ? "Consultation" : "General")
                .reasonForVisit(appointment.getReasonForVisit()).symptoms(appointment.getSymptoms())
                .diagnosis(appointment.getDiagnosis()).notes(appointment.getNotes()).duration(appointment.getDuration())
                .actualStartTime(appointment.getActualStartTime()).actualEndTime(appointment.getActualEndTime())
                .createdAt(appointment.getCreatedAt()).updatedAt(appointment.getUpdatedAt()).build();
    }
}
