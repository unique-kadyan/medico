package com.kaddy.service;

import com.kaddy.dto.EmergencyPatientDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Doctor;
import com.kaddy.model.EmergencyPatient;
import com.kaddy.model.EmergencyRoom;
import com.kaddy.model.Patient;
import com.kaddy.model.enums.PatientCondition;
import com.kaddy.repository.DoctorRepository;
import com.kaddy.repository.EmergencyPatientRepository;
import com.kaddy.repository.EmergencyRoomRepository;
import com.kaddy.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmergencyPatientService {

    private final EmergencyPatientRepository emergencyPatientRepository;
    private final PatientRepository patientRepository;
    private final EmergencyRoomRepository emergencyRoomRepository;
    private final DoctorRepository doctorRepository;
    private final EmergencyRoomService emergencyRoomService;
    private final ModelMapper modelMapper;

    @Transactional
    public EmergencyPatientDTO admitPatient(EmergencyPatientDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        EmergencyRoom room = emergencyRoomRepository.findById(dto.getEmergencyRoomId()).orElseThrow(
                () -> new ResourceNotFoundException("Emergency room not found with id: " + dto.getEmergencyRoomId()));

        if (room.getCurrentOccupancy() >= room.getCapacity()) {
            throw new IllegalStateException("Emergency room is at full capacity");
        }

        emergencyPatientRepository.findActiveByPatientId(dto.getPatientId()).ifPresent(ep -> {
            throw new IllegalStateException("Patient already has an active emergency admission");
        });

        Doctor attendingDoctor = null;
        if (dto.getAttendingDoctorId() != null) {
            attendingDoctor = doctorRepository.findById(dto.getAttendingDoctorId()).orElseThrow(
                    () -> new ResourceNotFoundException("Doctor not found with id: " + dto.getAttendingDoctorId()));
        }

        EmergencyPatient emergencyPatient = new EmergencyPatient();
        emergencyPatient.setPatient(patient);
        emergencyPatient.setEmergencyRoom(room);
        emergencyPatient.setAttendingDoctor(attendingDoctor);
        emergencyPatient.setCondition(dto.getCondition() != null ? dto.getCondition() : PatientCondition.STABLE);
        emergencyPatient.setTriageLevel(dto.getTriageLevel());
        emergencyPatient.setAdmissionTime(LocalDateTime.now());
        emergencyPatient.setChiefComplaint(dto.getChiefComplaint());
        emergencyPatient.setVitalSigns(dto.getVitalSigns());
        emergencyPatient.setTreatmentPlan(dto.getTreatmentPlan());
        emergencyPatient.setProgressNotes(dto.getMedicationsAdministered());
        emergencyPatient
                .setRequiresMonitoring(dto.getRequiresMonitoring() != null ? dto.getRequiresMonitoring() : false);

        EmergencyPatient saved = emergencyPatientRepository.save(emergencyPatient);

        emergencyRoomService.updateOccupancy(room.getId(), room.getCurrentOccupancy() + 1);

        return mapToDTO(saved);
    }

    public List<EmergencyPatientDTO> getAllEmergencyPatients() {
        return emergencyPatientRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<EmergencyPatientDTO> getCurrentPatients() {
        return emergencyPatientRepository.findCurrentPatients().stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyPatientDTO> getPatientsByRoom(Long roomId) {
        return emergencyPatientRepository.findActivePatientsByRoom(roomId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyPatientDTO> getPatientsByCondition(PatientCondition condition) {
        return emergencyPatientRepository.findActivePatientsByCondition(condition).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyPatientDTO> getPatientsRequiringMonitoring() {
        return emergencyPatientRepository.findPatientsRequiringMonitoring().stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyPatientDTO> getPatientsByDoctor(Long doctorId) {
        return emergencyPatientRepository.findByAttendingDoctorId(doctorId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyPatientDTO> getPatientHistory(Long patientId) {
        return emergencyPatientRepository.findByPatientId(patientId).stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EmergencyPatientDTO getEmergencyPatientById(Long id) {
        EmergencyPatient emergencyPatient = emergencyPatientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency patient record not found with id: " + id));
        return mapToDTO(emergencyPatient);
    }

    @Transactional
    public EmergencyPatientDTO updateEmergencyPatient(Long id, EmergencyPatientDTO dto) {
        EmergencyPatient existing = emergencyPatientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency patient record not found with id: " + id));

        if (existing.getDischargeTime() != null) {
            throw new IllegalStateException("Cannot update discharged patient record");
        }

        if (dto.getAttendingDoctorId() != null && !dto.getAttendingDoctorId()
                .equals(existing.getAttendingDoctor() != null ? existing.getAttendingDoctor().getId() : null)) {
            Doctor doctor = doctorRepository.findById(dto.getAttendingDoctorId()).orElseThrow(
                    () -> new ResourceNotFoundException("Doctor not found with id: " + dto.getAttendingDoctorId()));
            existing.setAttendingDoctor(doctor);
        }

        if (dto.getCondition() != null) {
            existing.setCondition(dto.getCondition());
        }
        if (dto.getTriageLevel() != null) {
            existing.setTriageLevel(dto.getTriageLevel());
        }
        if (dto.getVitalSigns() != null) {
            existing.setVitalSigns(dto.getVitalSigns());
        }
        if (dto.getTreatmentPlan() != null) {
            existing.setTreatmentPlan(dto.getTreatmentPlan());
        }
        if (dto.getMedicationsAdministered() != null) {
            existing.setProgressNotes(dto.getMedicationsAdministered());
        }
        if (dto.getRequiresMonitoring() != null) {
            existing.setRequiresMonitoring(dto.getRequiresMonitoring());
        }
        if (dto.getNotes() != null) {
            existing.setProgressNotes(dto.getNotes());
        }

        EmergencyPatient updated = emergencyPatientRepository.save(existing);
        return mapToDTO(updated);
    }

    @Transactional
    public EmergencyPatientDTO updateCondition(Long id, PatientCondition condition) {
        EmergencyPatient emergencyPatient = emergencyPatientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency patient record not found with id: " + id));

        if (emergencyPatient.getDischargeTime() != null) {
            throw new IllegalStateException("Cannot update discharged patient");
        }

        emergencyPatient.setCondition(condition);
        EmergencyPatient updated = emergencyPatientRepository.save(emergencyPatient);
        return mapToDTO(updated);
    }

    @Transactional
    public EmergencyPatientDTO dischargePatient(Long id, String dischargeNotes) {
        EmergencyPatient emergencyPatient = emergencyPatientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency patient record not found with id: " + id));

        if (emergencyPatient.getDischargeTime() != null) {
            throw new IllegalStateException("Patient already discharged");
        }

        emergencyPatient.setDischargeTime(LocalDateTime.now());
        emergencyPatient.setCondition(PatientCondition.DISCHARGED);
        if (dischargeNotes != null) {
            emergencyPatient.setDischargeNotes(dischargeNotes);
        }

        EmergencyPatient updated = emergencyPatientRepository.save(emergencyPatient);

        EmergencyRoom room = emergencyPatient.getEmergencyRoom();
        if (room.getCurrentOccupancy() > 0) {
            emergencyRoomService.updateOccupancy(room.getId(), room.getCurrentOccupancy() - 1);
        }

        return mapToDTO(updated);
    }

    @Transactional
    public EmergencyPatientDTO transferToRoom(Long id, Long newRoomId) {
        EmergencyPatient emergencyPatient = emergencyPatientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency patient record not found with id: " + id));

        if (emergencyPatient.getDischargeTime() != null) {
            throw new IllegalStateException("Cannot transfer discharged patient");
        }

        EmergencyRoom newRoom = emergencyRoomRepository.findById(newRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency room not found with id: " + newRoomId));

        if (newRoom.getCurrentOccupancy() >= newRoom.getCapacity()) {
            throw new IllegalStateException("Target emergency room is at full capacity");
        }

        EmergencyRoom oldRoom = emergencyPatient.getEmergencyRoom();

        emergencyRoomService.updateOccupancy(oldRoom.getId(), oldRoom.getCurrentOccupancy() - 1);
        emergencyRoomService.updateOccupancy(newRoom.getId(), newRoom.getCurrentOccupancy() + 1);

        emergencyPatient.setEmergencyRoom(newRoom);
        EmergencyPatient updated = emergencyPatientRepository.save(emergencyPatient);

        return mapToDTO(updated);
    }

    private EmergencyPatientDTO mapToDTO(EmergencyPatient emergencyPatient) {
        EmergencyPatientDTO dto = modelMapper.map(emergencyPatient, EmergencyPatientDTO.class);
        dto.setPatientId(emergencyPatient.getPatient().getId());
        dto.setPatientName(
                emergencyPatient.getPatient().getFirstName() + " " + emergencyPatient.getPatient().getLastName());
        dto.setEmergencyRoomId(emergencyPatient.getEmergencyRoom().getId());
        dto.setEmergencyRoomNumber(emergencyPatient.getEmergencyRoom().getRoomNumber());

        if (emergencyPatient.getAttendingDoctor() != null) {
            dto.setAttendingDoctorId(emergencyPatient.getAttendingDoctor().getId());
            dto.setAttendingDoctorName(emergencyPatient.getAttendingDoctor().getFirstName() + " "
                    + emergencyPatient.getAttendingDoctor().getLastName());
        }

        return dto;
    }
}
