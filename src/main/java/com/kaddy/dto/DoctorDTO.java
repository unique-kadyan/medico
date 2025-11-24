package com.kaddy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {

    private Long id;

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    private String licenseNumber;

    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String department;

    @NotNull(message = "Years of experience is required")
    private Integer yearsOfExperience;

    private String qualification;

    private String about;

    private Boolean availableForConsultation;

    private Boolean active;

    private String createdAt;

    private String updatedAt;

    private String fullName;

    public String getFullName() {
        return "Dr. " + firstName + " " + lastName;
    }
}
