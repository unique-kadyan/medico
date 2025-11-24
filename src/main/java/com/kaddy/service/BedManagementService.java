package com.kaddy.service;

import com.kaddy.dto.BedDTO;
import com.kaddy.dto.WardDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Bed;
import com.kaddy.model.Hospital;
import com.kaddy.model.Patient;
import com.kaddy.model.Ward;
import com.kaddy.model.enums.BedStatus;
import com.kaddy.model.enums.BedType;
import com.kaddy.repository.BedRepository;
import com.kaddy.repository.HospitalRepository;
import com.kaddy.repository.PatientRepository;
import com.kaddy.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BedManagementService {

    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public WardDTO createWard(Long hospitalId, WardDTO wardDTO) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));

        if (wardRepository.existsByHospitalIdAndCode(hospitalId, wardDTO.getCode())) {
            throw new IllegalArgumentException("Ward with this code already exists");
        }

        Ward ward = new Ward();
        ward.setName(wardDTO.getName());
        ward.setCode(wardDTO.getCode());
        ward.setHospital(hospital);
        ward.setWardType(wardDTO.getWardType());
        ward.setFloorNumber(wardDTO.getFloorNumber());
        ward.setDescription(wardDTO.getDescription());
        ward.setNurseStation(wardDTO.getNurseStation());
        ward.setTotalBeds(0);
        ward.setAvailableBeds(0);

        return mapWardToDTO(wardRepository.save(ward));
    }

    @Transactional(readOnly = true)
    public List<WardDTO> getWardsByHospital(Long hospitalId) {
        return wardRepository.findByHospitalIdAndIsActiveTrue(hospitalId).stream().map(this::mapWardToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WardDTO getWardById(Long wardId) {
        Ward ward = wardRepository.findById(wardId).orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        return mapWardToDTO(ward);
    }

    @Transactional
    public WardDTO updateWard(Long wardId, WardDTO wardDTO) {
        Ward ward = wardRepository.findById(wardId).orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        ward.setName(wardDTO.getName());
        ward.setWardType(wardDTO.getWardType());
        ward.setFloorNumber(wardDTO.getFloorNumber());
        ward.setDescription(wardDTO.getDescription());
        ward.setNurseStation(wardDTO.getNurseStation());

        return mapWardToDTO(wardRepository.save(ward));
    }

    @Transactional
    public BedDTO createBed(Long hospitalId, BedDTO bedDTO) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));

        Ward ward = wardRepository.findById(bedDTO.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        if (bedRepository.existsByHospitalIdAndBedNumber(hospitalId, bedDTO.getBedNumber())) {
            throw new IllegalArgumentException("Bed with this number already exists");
        }

        Bed bed = new Bed();
        bed.setBedNumber(bedDTO.getBedNumber());
        bed.setWard(ward);
        bed.setHospital(hospital);
        bed.setBedType(bedDTO.getBedType());
        bed.setStatus(BedStatus.AVAILABLE);
        bed.setDailyRate(bedDTO.getDailyRate());
        bed.setFeatures(bedDTO.getFeatures());
        bed.setFloorNumber(bedDTO.getFloorNumber());
        bed.setRoomNumber(bedDTO.getRoomNumber());
        bed.setNotes(bedDTO.getNotes());

        Bed savedBed = bedRepository.save(bed);

        ward.setTotalBeds(ward.getTotalBeds() + 1);
        ward.setAvailableBeds(ward.getAvailableBeds() + 1);
        wardRepository.save(ward);

        log.info("Created bed {} in ward {}", bedDTO.getBedNumber(), ward.getName());
        return mapBedToDTO(savedBed);
    }

    @Transactional(readOnly = true)
    public List<BedDTO> getBedsByHospital(Long hospitalId) {
        return bedRepository.findByHospitalIdAndActiveTrue(hospitalId).stream().map(this::mapBedToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BedDTO> getBedsByWard(Long wardId) {
        return bedRepository.findByWardIdAndActiveTrue(wardId).stream().map(this::mapBedToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BedDTO> getAvailableBeds(Long hospitalId) {
        return bedRepository.findByHospitalIdAndStatusAndActiveTrue(hospitalId, BedStatus.AVAILABLE).stream()
                .map(this::mapBedToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BedDTO> getAvailableBedsByType(Long hospitalId, BedType bedType) {
        return bedRepository.findAvailableBedsByType(hospitalId, bedType).stream().map(this::mapBedToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BedDTO> getAvailableBedsByWard(Long wardId) {
        return bedRepository.findAvailableBedsByWard(wardId).stream().map(this::mapBedToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BedDTO assignPatientToBed(Long bedId, Long patientId) {
        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new ResourceNotFoundException("Bed not found"));

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalStateException("Bed is not available");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        bed.setCurrentPatient(patient);
        bed.setStatus(BedStatus.OCCUPIED);

        Ward ward = bed.getWard();
        ward.setAvailableBeds(ward.getAvailableBeds() - 1);
        wardRepository.save(ward);

        log.info("Assigned patient {} to bed {}", patientId, bed.getBedNumber());
        return mapBedToDTO(bedRepository.save(bed));
    }

    @Transactional
    public BedDTO releaseBed(Long bedId) {
        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new ResourceNotFoundException("Bed not found"));

        bed.setCurrentPatient(null);
        bed.setStatus(BedStatus.CLEANING);

        log.info("Released bed {}", bed.getBedNumber());
        return mapBedToDTO(bedRepository.save(bed));
    }

    @Transactional
    public BedDTO updateBedStatus(Long bedId, BedStatus newStatus) {
        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new ResourceNotFoundException("Bed not found"));

        BedStatus oldStatus = bed.getStatus();
        bed.setStatus(newStatus);

        if (newStatus == BedStatus.AVAILABLE && oldStatus == BedStatus.CLEANING) {
            bed.setLastCleanedAt(LocalDateTime.now());
        } else if (newStatus == BedStatus.AVAILABLE && oldStatus == BedStatus.MAINTENANCE) {
            bed.setLastMaintenanceAt(LocalDateTime.now());
        }

        Ward ward = bed.getWard();
        if (oldStatus == BedStatus.OCCUPIED && newStatus != BedStatus.OCCUPIED) {
            ward.setAvailableBeds(ward.getAvailableBeds() + 1);
        } else if (oldStatus != BedStatus.OCCUPIED && newStatus == BedStatus.OCCUPIED) {
            ward.setAvailableBeds(ward.getAvailableBeds() - 1);
        }
        wardRepository.save(ward);

        log.info("Updated bed {} status from {} to {}", bed.getBedNumber(), oldStatus, newStatus);
        return mapBedToDTO(bedRepository.save(bed));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getBedStatistics(Long hospitalId) {
        Map<String, Object> stats = new HashMap<>();

        Integer totalBeds = wardRepository.getTotalBedsByHospital(hospitalId);
        Integer availableBeds = wardRepository.getAvailableBedsByHospital(hospitalId);

        stats.put("totalBeds", totalBeds != null ? totalBeds : 0);
        stats.put("availableBeds", availableBeds != null ? availableBeds : 0);
        stats.put("occupiedBeds", (totalBeds != null ? totalBeds : 0) - (availableBeds != null ? availableBeds : 0));
        stats.put("occupancyRate",
                totalBeds != null && totalBeds > 0
                        ? ((totalBeds - (availableBeds != null ? availableBeds : 0)) * 100.0 / totalBeds)
                        : 0);

        for (BedStatus status : BedStatus.values()) {
            long count = bedRepository.countByHospitalIdAndStatus(hospitalId, status);
            stats.put("beds" + status.name(), count);
        }

        for (BedType type : BedType.values()) {
            List<Bed> beds = bedRepository.findByHospitalIdAndBedTypeAndActiveTrue(hospitalId, type);
            long available = beds.stream().filter(b -> b.getStatus() == BedStatus.AVAILABLE).count();
            stats.put(type.name().toLowerCase() + "Total", beds.size());
            stats.put(type.name().toLowerCase() + "Available", available);
        }

        return stats;
    }

    private WardDTO mapWardToDTO(Ward ward) {
        WardDTO dto = modelMapper.map(ward, WardDTO.class);
        dto.setOccupiedBeds(ward.getTotalBeds() - ward.getAvailableBeds());
        return dto;
    }

    private BedDTO mapBedToDTO(Bed bed) {
        BedDTO dto = modelMapper.map(bed, BedDTO.class);
        dto.setWardId(bed.getWard().getId());
        dto.setWardName(bed.getWard().getName());
        dto.setHospitalId(bed.getHospital().getId());
        if (bed.getCurrentPatient() != null) {
            dto.setCurrentPatientId(bed.getCurrentPatient().getId());
            dto.setCurrentPatientName(
                    bed.getCurrentPatient().getFirstName() + " " + bed.getCurrentPatient().getLastName());
        }
        return dto;
    }
}
