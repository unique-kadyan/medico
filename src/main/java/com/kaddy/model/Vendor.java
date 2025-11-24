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
@Table(name = "vendors", indexes = {@Index(name = "idx_vendor_hospital", columnList = "hospital_id"),
        @Index(name = "idx_vendor_code", columnList = "vendorCode")})
public class Vendor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String vendorCode;

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

    private String gstNumber;
    private String panNumber;
    private String drugLicenseNumber;

    private String bankName;
    private String bankAccountNumber;
    private String bankIfscCode;

    private Integer creditDays = 30;

    @Column(precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(precision = 12, scale = 2)
    private BigDecimal currentOutstanding = BigDecimal.ZERO;

    private Integer rating;

    @Column(length = 500)
    private String notes;

    private Boolean isActive = true;
}
