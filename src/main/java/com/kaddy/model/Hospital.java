package com.kaddy.model;

import com.kaddy.model.enums.SubscriptionPlan;
import com.kaddy.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hospitals")
public class Hospital extends BaseEntity {

    @NotBlank(message = "Hospital name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Hospital code is required")
    @Column(unique = true, nullable = false)
    private String code;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(length = 500)
    private String address;

    private String city;
    private String state;
    private String country;
    private String postalCode;

    @Column(length = 50)
    private String registrationNumber;

    @Column(length = 100)
    private String taxId;

    private String logoUrl;
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.TRIAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime trialStartDate;

    @Column(nullable = false)
    private LocalDateTime trialEndDate;

    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;

    private String stripeCustomerId;
    private String stripeSubscriptionId;

    @Column(nullable = false)
    private Integer maxUsers = 10;

    @Column(nullable = false)
    private Integer maxPatients = 100;

    @Column(nullable = false)
    private Boolean aiEnabled = false;

    @Column(nullable = false)
    private Boolean fhirEnabled = false;

    @Column(nullable = false)
    private Boolean bedManagementEnabled = true;

    public boolean isTrialExpired() {
        return LocalDateTime.now().isAfter(trialEndDate) && subscriptionPlan == SubscriptionPlan.TRIAL;
    }

    public boolean isSubscriptionActive() {
        if (subscriptionPlan == SubscriptionPlan.TRIAL) {
            return !isTrialExpired();
        }
        return subscriptionStatus == SubscriptionStatus.ACTIVE
                && (subscriptionEndDate == null || LocalDateTime.now().isBefore(subscriptionEndDate));
    }

    public long getDaysRemainingInTrial() {
        if (subscriptionPlan != SubscriptionPlan.TRIAL) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), trialEndDate).toDays();
    }
}
