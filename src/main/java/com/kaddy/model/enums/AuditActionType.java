package com.kaddy.model.enums;

public enum AuditActionType {
    CONSENT_REQUESTED("Consent was requested"), CONSENT_GRANTED("Patient granted consent"), CONSENT_DENIED(
            "Patient denied consent"), CONSENT_REVOKED("Patient revoked consent"), CONSENT_EXPIRED(
                    "Consent expired automatically"), RECORD_ACCESSED("Medical record was accessed"), RECORD_SHARED(
                            "Medical record was shared"), RECORD_EXPORTED(
                                    "Medical record was exported"), SHARE_REQUEST_CREATED(
                                            "Record sharing request created"), SHARE_REQUEST_APPROVED(
                                                    "Record sharing request approved"), SHARE_REQUEST_DENIED(
                                                            "Record sharing request denied"), SHARE_REQUEST_CANCELLED(
                                                                    "Record sharing request cancelled");

    private final String description;

    AuditActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
