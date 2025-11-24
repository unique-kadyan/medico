package com.kaddy.controller;

import com.kaddy.dto.EmergencyRoomDTO;
import com.kaddy.model.enums.EmergencyRoomStatus;
import com.kaddy.service.EmergencyRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency-rooms")
@RequiredArgsConstructor
public class EmergencyRoomController {

    private final EmergencyRoomService emergencyRoomService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<EmergencyRoomDTO> createEmergencyRoom(@RequestBody EmergencyRoomDTO dto) {
        EmergencyRoomDTO created = emergencyRoomService.createEmergencyRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyRoomDTO>> getAllEmergencyRooms() {
        List<EmergencyRoomDTO> rooms = emergencyRoomService.getAllEmergencyRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyRoomDTO>> getActiveEmergencyRooms() {
        List<EmergencyRoomDTO> rooms = emergencyRoomService.getActiveEmergencyRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyRoomDTO>> getAvailableRooms() {
        List<EmergencyRoomDTO> rooms = emergencyRoomService.getAvailableRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyRoomDTO>> getRoomsByStatus(@PathVariable EmergencyRoomStatus status) {
        List<EmergencyRoomDTO> rooms = emergencyRoomService.getRoomsByStatus(status);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/floor/{floorNumber}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<List<EmergencyRoomDTO>> getRoomsByFloor(@PathVariable Integer floorNumber) {
        List<EmergencyRoomDTO> rooms = emergencyRoomService.getRoomsByFloor(floorNumber);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyRoomDTO> getEmergencyRoomById(@PathVariable Long id) {
        EmergencyRoomDTO room = emergencyRoomService.getEmergencyRoomById(id);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/room-number/{roomNumber}")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyRoomDTO> getEmergencyRoomByNumber(@PathVariable String roomNumber) {
        EmergencyRoomDTO room = emergencyRoomService.getEmergencyRoomByNumber(roomNumber);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/stats/available-count")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getAvailableRoomCount() {
        long count = emergencyRoomService.getAvailableRoomCount();
        return ResponseEntity.ok(Map.of("availableCount", count));
    }

    @GetMapping("/stats/occupied-count")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'NURSE', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getOccupiedRoomCount() {
        long count = emergencyRoomService.getOccupiedRoomCount();
        return ResponseEntity.ok(Map.of("occupiedCount", count));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyRoomDTO> updateEmergencyRoom(@PathVariable Long id,
            @RequestBody EmergencyRoomDTO dto) {
        EmergencyRoomDTO updated = emergencyRoomService.updateEmergencyRoom(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyRoomDTO> updateRoomStatus(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        EmergencyRoomStatus status = EmergencyRoomStatus.valueOf(body.get("status"));
        EmergencyRoomDTO updated = emergencyRoomService.updateRoomStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/occupancy")
    @PreAuthorize("hasAnyAuthority('NURSE', 'ADMIN')")
    public ResponseEntity<EmergencyRoomDTO> updateOccupancy(@PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer occupancy = body.get("occupancy");
        EmergencyRoomDTO updated = emergencyRoomService.updateOccupancy(id, occupancy);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deactivateEmergencyRoom(@PathVariable Long id) {
        emergencyRoomService.deactivateEmergencyRoom(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteEmergencyRoom(@PathVariable Long id) {
        emergencyRoomService.deleteEmergencyRoom(id);
        return ResponseEntity.noContent().build();
    }
}
