package com.kaddy.service;

import com.kaddy.dto.DoctorDTO;
import com.kaddy.exception.DuplicateResourceException;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Doctor;
import com.kaddy.repository.DoctorRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;

    public DoctorService(DoctorRepository doctorRepository, ModelMapper modelMapper) {
        this.doctorRepository = doctorRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllActiveDoctors() {
        return doctorRepository.findAllActiveDoctorsOrderedByExperience().stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAvailableDoctors() {
        return doctorRepository.findAllAvailableDoctors().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return convertToDTO(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorDTO getDoctorByDoctorId(String doctorId) {
        Doctor doctor = doctorRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with doctor ID: " + doctorId));
        return convertToDTO(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsByDepartment(String department) {
        return doctorRepository.findByDepartment(department).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DoctorDTO createDoctor(DoctorDTO doctorDTO) {
        if (doctorRepository.existsByDoctorId(doctorDTO.getDoctorId())) {
            throw new DuplicateResourceException("Doctor with ID " + doctorDTO.getDoctorId() + " already exists");
        }

        if (doctorDTO.getLicenseNumber() != null
                && doctorRepository.existsByLicenseNumber(doctorDTO.getLicenseNumber())) {
            throw new DuplicateResourceException(
                    "Doctor with license number " + doctorDTO.getLicenseNumber() + " already exists");
        }

        Doctor doctor = convertToEntity(doctorDTO);

        if (doctor.getYearsOfExperience() == null) {
            doctor.setYearsOfExperience(0);
        }
        if (doctor.getAvailableForConsultation() == null) {
            doctor.setAvailableForConsultation(true);
        }
        if (doctor.getActive() == null) {
            doctor.setActive(true);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        return convertToDTO(savedDoctor);
    }

    public DoctorDTO updateDoctor(Long id, DoctorDTO doctorDTO) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        if (!existingDoctor.getDoctorId().equals(doctorDTO.getDoctorId())
                && doctorRepository.existsByDoctorId(doctorDTO.getDoctorId())) {
            throw new DuplicateResourceException("Doctor with ID " + doctorDTO.getDoctorId() + " already exists");
        }

        if (doctorDTO.getLicenseNumber() != null
                && !doctorDTO.getLicenseNumber().equals(existingDoctor.getLicenseNumber())
                && doctorRepository.existsByLicenseNumber(doctorDTO.getLicenseNumber())) {
            throw new DuplicateResourceException(
                    "Doctor with license number " + doctorDTO.getLicenseNumber() + " already exists");
        }

        existingDoctor.setDoctorId(doctorDTO.getDoctorId());
        existingDoctor.setFirstName(doctorDTO.getFirstName());
        existingDoctor.setLastName(doctorDTO.getLastName());
        existingDoctor.setSpecialization(doctorDTO.getSpecialization());
        existingDoctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        existingDoctor.setPhone(doctorDTO.getPhone());
        existingDoctor.setEmail(doctorDTO.getEmail());
        existingDoctor.setDepartment(doctorDTO.getDepartment());
        existingDoctor.setYearsOfExperience(doctorDTO.getYearsOfExperience());
        existingDoctor.setQualification(doctorDTO.getQualification());
        existingDoctor.setAbout(doctorDTO.getAbout());

        if (doctorDTO.getAvailableForConsultation() != null) {
            existingDoctor.setAvailableForConsultation(doctorDTO.getAvailableForConsultation());
        }
        if (doctorDTO.getActive() != null) {
            existingDoctor.setActive(doctorDTO.getActive());
        }

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return convertToDTO(updatedDoctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        doctorRepository.delete(doctor);
    }

    public DoctorDTO updateAvailability(Long id, boolean available) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        doctor.setAvailableForConsultation(available);
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return convertToDTO(updatedDoctor);
    }

    public DoctorDTO activateDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        doctor.setActive(true);
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return convertToDTO(updatedDoctor);
    }

    public DoctorDTO deactivateDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        doctor.setActive(false);
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return convertToDTO(updatedDoctor);
    }

    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = modelMapper.map(doctor, DoctorDTO.class);
        dto.setFullName(doctor.getFullName());
        return dto;
    }

    private Doctor convertToEntity(DoctorDTO doctorDTO) {
        return modelMapper.map(doctorDTO, Doctor.class);
    }
}
