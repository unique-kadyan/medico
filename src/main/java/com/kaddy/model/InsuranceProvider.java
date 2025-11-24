package com.kaddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "insurance_providers", indexes = {
        @Index(name = "idx_insurance_provider_code", columnList = "providerCode", unique = true)})
public class InsuranceProvider extends BaseEntity {

    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    private String providerCode;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    private String city;
    private String state;
    private String country;
    private String postalCode;

    private String phone;
    private String email;
    private String website;

    private String contactPerson;
    private String contactPhone;
    private String contactEmail;

    private String tpaName;
    private String tpaCode;

    @Column(precision = 5, scale = 2)
    private BigDecimal defaultCoveragePercentage = BigDecimal.valueOf(80);

    private Integer claimProcessingDays = 30;

    @Column(length = 500)
    private String notes;

    private Boolean isActive = true;
}
