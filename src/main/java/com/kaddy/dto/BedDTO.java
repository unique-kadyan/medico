package com.kaddy.dto;

import com.kaddy.model.enums.BedStatus;
import com.kaddy.model.enums.BedType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BedDTO {
    private Long id;
    private String bedNumber;
    private Long wardId;
    private String wardName;
    private Long hospitalId;
    private BedType bedType;
    private BedStatus status;
    private Long currentPatientId;
    private String currentPatientName;
    private BigDecimal dailyRate;
    private String features;
    private LocalDateTime lastCleanedAt;
    private LocalDateTime lastMaintenanceAt;
    private String notes;
    private Integer floorNumber;
    private String roomNumber;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
