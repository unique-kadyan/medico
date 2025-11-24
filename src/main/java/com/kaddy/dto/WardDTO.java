package com.kaddy.dto;

import com.kaddy.model.enums.WardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardDTO {
    private Long id;
    private String name;
    private String code;
    private Long hospitalId;
    private WardType wardType;
    private Integer floorNumber;
    private Integer totalBeds;
    private Integer availableBeds;
    private Integer occupiedBeds;
    private String description;
    private String nurseStation;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
