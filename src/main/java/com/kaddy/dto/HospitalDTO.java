package com.kaddy.dto;

import com.kaddy.model.enums.SubscriptionPlan;
import com.kaddy.model.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDTO {
    private Long id;
    private String name;
    private String code;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String registrationNumber;
    private String taxId;
    private String logoUrl;
    private String website;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime trialStartDate;
    private LocalDateTime trialEndDate;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private Integer maxUsers;
    private Integer maxPatients;
    private Boolean aiEnabled;
    private Boolean fhirEnabled;
    private Boolean bedManagementEnabled;
    private Long daysRemainingInTrial;
    private Boolean isTrialExpired;
    private Boolean isSubscriptionActive;
    private Long currentUserCount;
    private Long currentPatientCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}
