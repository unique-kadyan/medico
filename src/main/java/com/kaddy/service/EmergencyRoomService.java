package com.kaddy.service;

import com.kaddy.dto.EmergencyRoomDTO;
import com.kaddy.exception.DuplicateResourceException;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.EmergencyRoom;
import com.kaddy.model.enums.EmergencyRoomStatus;
import com.kaddy.repository.EmergencyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmergencyRoomService {

    private final EmergencyRoomRepository emergencyRoomRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public EmergencyRoomDTO createEmergencyRoom(EmergencyRoomDTO dto) {
        if (emergencyRoomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new DuplicateResourceException("Emergency room with number " + dto.getRoomNumber() + " already exists");
        }

        EmergencyRoom room = new EmergencyRoom();
        room.setRoomNumber(dto.getRoomNumber());
        room.setStatus(dto.getStatus() != null ? dto.getStatus() : EmergencyRoomStatus.AVAILABLE);
        room.setLocation(dto.getLocation());
        room.setFloorNumber(dto.getFloorNumber());
        room.setEquipment(dto.getEquipment());
        room.setCapacity(dto.getCapacity() != null ? dto.getCapacity() : 1);
        room.setCurrentOccupancy(0);
        room.setNotes(dto.getNotes());
        room.setIsActive(true);

        EmergencyRoom saved = emergencyRoomRepository.save(room);
        return mapToDTO(saved);
    }

    public List<EmergencyRoomDTO> getAllEmergencyRooms() {
        return emergencyRoomRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyRoomDTO> getActiveEmergencyRooms() {
        return emergencyRoomRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyRoomDTO> getAvailableRooms() {
        return emergencyRoomRepository.findAvailableRooms().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyRoomDTO> getRoomsByStatus(EmergencyRoomStatus status) {
        return emergencyRoomRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmergencyRoomDTO> getRoomsByFloor(Integer floorNumber) {
        return emergencyRoomRepository.findByFloorNumber(floorNumber).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EmergencyRoomDTO getEmergencyRoomById(Long id) {
        EmergencyRoom room = emergencyRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency room not found with id: " + id));
        return mapToDTO(room);
    }

    public EmergencyRoomDTO getEmergencyRoomByNumber(String roomNumber) {
        EmergencyRoom room = emergencyRoomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency room not found with number: " + roomNumber));
        return mapToDTO(room);
    }

    public long getAvailableRoomCount() {
        return emergencyRoomRepository.countAvailableRooms();
    }

    public long getOccupiedRoomCount() {
        return emergencyRoomRepository.countOccupiedRooms();
    }

    @Transactional
    public EmergencyRoomDTO updateEmergencyRoom(Long id, EmergencyRoomDTO dto) {
        EmergencyRoom existing = emergencyRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency room not found with id: " + id));

        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        if (dto.getLocation() != null) {
            existing.setLocation(dto.getLocation());
        }
        if (dto.getFloorNumber() != null) {
            existing.setFloorNumber(dto.getFloorNumber());
        }
        if (dto.getEquipment() != null) {
            existing.setEquipment(dto.getEquipment());
        }
        if (dto.getCapacity() != null) {
            existing.setCapacity(dto.getCapacity());
        }
        if (dto.getNotes() != null) {
            existing.setNotes(dto.getNotes());
        }

        EmergencyRoom updated = emergencyRoomRepository.save(existing);
        return mapToDTO(updated);
    }

    @Transactional
    public EmergencyRoomDTO updateRoomStatus(Long id, EmergencyRoomStatus status) {
        EmergencyRoom room = emergencyRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency room not found with id: " + id));

        room.setStatus(status);
        EmergencyRoom updated = emergencyRoomRepository.save(room);
        return mapToDTO(updated);
    }

    @Transactional
    public EmergencyRoomDTO updateOccupancy(Long id, Integer occupancy) {
        EmergencyRoom room = emergencyRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency room not found with id: " + id));

        if (occupancy < 0 || occupancy > room.getCapacity()) {
            throw new IllegalArgumentException("Invalid occupancy value. Must be between 0 and " + room.getCapacity());
        }

        room.setCurrentOccupancy(occupancy);

        if (occupancy == 0) {
            room.setStatus(EmergencyRoomStatus.AVAILABLE);
        } else if (occupancy >= room.getCapacity()) {
            room.setStatus(EmergencyRoomStatus.OCCUPIED);
        }

        EmergencyRoom updated = emergencyRoomRepository.save(room);
        return mapToDTO(updated);
    }

    @Transactional
    public void deactivateEmergencyRoom(Long id) {
        EmergencyRoom room = emergencyRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency room not found with id: " + id));

        if (room.getCurrentOccupancy() > 0) {
            throw new IllegalStateException("Cannot deactivate room with current occupancy");
        }

        room.setIsActive(false);
        room.setStatus(EmergencyRoomStatus.MAINTENANCE);
        emergencyRoomRepository.save(room);
    }

    @Transactional
    public void deleteEmergencyRoom(Long id) {
        EmergencyRoom room = emergencyRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency room not found with id: " + id));

        if (room.getCurrentOccupancy() > 0) {
            throw new IllegalStateException("Cannot delete room with current occupancy");
        }

        emergencyRoomRepository.delete(room);
    }

    private EmergencyRoomDTO mapToDTO(EmergencyRoom room) {
        return modelMapper.map(room, EmergencyRoomDTO.class);
    }
}
