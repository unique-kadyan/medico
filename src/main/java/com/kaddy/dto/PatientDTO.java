package com.kaddy.dto;

import com.kaddy.model.enums.BloodGroup;
import com.kaddy.model.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private Long id;

    @NotBlank(message = "Patient ID is required")
    private String patientId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    private String address;
    private String emergencyContact;
    private String emergencyContactPhone;
    private BloodGroup bloodGroup;
    private String allergies;
    private String chronicConditions;
    private Integer age;
    private String fullName;
    private Boolean active;
}
