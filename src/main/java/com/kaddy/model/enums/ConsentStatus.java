package com.kaddy.model.enums;

public enum ConsentStatus {
    PENDING("Pending patient approval"), APPROVED("Patient has granted consent"), DENIED(
            "Patient has denied consent"), REVOKED(
                    "Patient has revoked previously granted consent"), EXPIRED("Consent has expired");

    private final String description;

    ConsentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
