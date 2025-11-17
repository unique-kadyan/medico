package com.kaddy.repository;

import com.kaddy.model.EmergencyRoom;
import com.kaddy.model.enums.EmergencyRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRoomRepository extends JpaRepository<EmergencyRoom, Long> {

    Optional<EmergencyRoom> findByRoomNumber(String roomNumber);

    List<EmergencyRoom> findByStatus(EmergencyRoomStatus status);

    List<EmergencyRoom> findByIsActiveTrue();

    @Query("SELECT e FROM EmergencyRoom e WHERE e.isActive = true AND e.status = 'AVAILABLE' ORDER BY e.roomNumber")
    List<EmergencyRoom> findAvailableRooms();

    @Query("SELECT e FROM EmergencyRoom e WHERE e.floorNumber = :floorNumber AND e.isActive = true")
    List<EmergencyRoom> findByFloorNumber(@Param("floorNumber") Integer floorNumber);

    @Query("SELECT COUNT(e) FROM EmergencyRoom e WHERE e.status = 'AVAILABLE' AND e.isActive = true")
    long countAvailableRooms();

    @Query("SELECT COUNT(e) FROM EmergencyRoom e WHERE e.status = 'OCCUPIED' AND e.isActive = true")
    long countOccupiedRooms();

    boolean existsByRoomNumber(String roomNumber);
}
