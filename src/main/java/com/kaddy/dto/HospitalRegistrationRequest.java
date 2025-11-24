package com.kaddy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalRegistrationRequest {

    @NotBlank(message = "Hospital name is required")
    @Size(min = 2, max = 200, message = "Hospital name must be between 2 and 200 characters")
    private String hospitalName;

    @NotBlank(message = "Hospital email is required")
    @Email(message = "Hospital email should be valid")
    private String hospitalEmail;

    private String hospitalPhone;
    private String hospitalAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String registrationNumber;
    private String taxId;
    private String website;

    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    private String adminLastName;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email should be valid")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminPassword;

    private String adminPhone;
}
