package com.kaddy.dto;

import com.kaddy.model.enums.EmergencyRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyRoomDTO {
    private Long id;
    private String roomNumber;
    private EmergencyRoomStatus status;
    private String location;
    private Integer floorNumber;
    private String equipment;
    private Integer capacity;
    private Integer currentOccupancy;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
