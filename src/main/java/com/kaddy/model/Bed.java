package com.kaddy.model;

import com.kaddy.model.enums.BedStatus;
import com.kaddy.model.enums.BedType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "beds")
public class Bed extends BaseEntity {

    @NotBlank(message = "Bed number is required")
    @Column(nullable = false)
    private String bedNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedType bedType = BedType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedStatus status = BedStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_patient_id")
    private Patient currentPatient;

    @Column(precision = 10, scale = 2)
    private BigDecimal dailyRate;

    private String features;

    private LocalDateTime lastCleanedAt;
    private LocalDateTime lastMaintenanceAt;

    private String notes;

    @Column(nullable = false)
    private Integer floorNumber = 1;

    private String roomNumber;

    public boolean isAvailable() {
        return status == BedStatus.AVAILABLE;
    }

    public boolean isOccupied() {
        return status == BedStatus.OCCUPIED;
    }
}
