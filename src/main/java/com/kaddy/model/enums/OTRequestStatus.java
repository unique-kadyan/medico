package com.kaddy.model.enums;

public enum OTRequestStatus {
    PENDING,        // Request submitted, waiting for approval
    APPROVED,       // Request approved, resources allocated
    REJECTED,       // Request rejected
    IN_PROGRESS,    // Surgery is currently ongoing
    COMPLETED,      // Surgery completed successfully
    CANCELLED       // Request cancelled by surgeon
}
