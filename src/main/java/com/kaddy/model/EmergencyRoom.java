package com.kaddy.model;

import com.kaddy.model.enums.EmergencyRoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emergency_rooms")
public class EmergencyRoom extends BaseEntity {

    @NotBlank(message = "Room number is required")
    @Column(unique = true, nullable = false)
    private String roomNumber;

    @Column
    private String roomName;

    @Column
    private Integer floorNumber;

    @Column
    private String location;

    @NotNull(message = "Room status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyRoomStatus status = EmergencyRoomStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String equipment;

    @Column
    private Integer capacity = 1;

    @Column
    private Integer currentOccupancy = 0;

    @Column
    private LocalDateTime lastCleanedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private Boolean isActive = true;
}
