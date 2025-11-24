package com.kaddy.model;

import com.kaddy.model.enums.ServiceCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_items", indexes = {@Index(name = "idx_service_item_hospital", columnList = "hospital_id"),
        @Index(name = "idx_service_item_code", columnList = "code"),
        @Index(name = "idx_service_item_category", columnList = "category")})
public class ServiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotBlank(message = "Service code is required")
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank(message = "Service name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceCategory category;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 20)
    private String unit;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(length = 50)
    private String hsnSacCode;

    private Boolean isActive = true;

    @Column(length = 50)
    private String insuranceCode;
}
