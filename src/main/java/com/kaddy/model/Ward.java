package com.kaddy.model;

import com.kaddy.model.enums.WardType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wards")
public class Ward extends BaseEntity {

    @NotBlank(message = "Ward name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Ward code is required")
    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WardType wardType = WardType.GENERAL;

    @Column(nullable = false)
    private Integer floorNumber = 1;

    @Column(nullable = false)
    private Integer totalBeds = 0;

    @Column(nullable = false)
    private Integer availableBeds = 0;

    private String description;

    private String nurseStation;

    @Column(nullable = false)
    private Boolean isActive = true;
}
