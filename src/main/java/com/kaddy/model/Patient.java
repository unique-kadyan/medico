package com.kaddy.model;

import com.kaddy.model.enums.BloodGroup;
import com.kaddy.model.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patients")
public class Patient extends BaseEntity {

    @NotBlank(message = "Patient ID is required")
    @Column(unique = true, nullable = false)
    private String patientId;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(length = 15)
    private String phone;

    @Column
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String emergencyContact;

    @Column(length = 15)
    private String emergencyContactPhone;

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String chronicConditions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Prescription> prescriptions = new ArrayList<>();

    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
