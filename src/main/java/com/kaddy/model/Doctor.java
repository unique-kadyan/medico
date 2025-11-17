package com.kaddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor extends BaseEntity {

    @NotBlank(message = "Doctor ID is required")
    @Column(unique = true, nullable = false)
    private String doctorId;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Specialization is required")
    @Column(nullable = false)
    private String specialization;

    @Column(unique = true)
    private String licenseNumber;

    @Column(length = 15)
    private String phone;

    @Column
    private String email;

    @Column
    private String department;

    @Column(nullable = false)
    private Integer yearsOfExperience = 0;

    @Column
    private String qualification;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(nullable = false)
    private Boolean availableForConsultation = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> appointments = new ArrayList<>();

    public String getFullName() {
        return "Dr. " + firstName + " " + lastName;
    }
}
