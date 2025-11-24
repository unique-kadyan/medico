package com.kaddy.model.enums;

public enum StockMovementType {
    PURCHASE, // Stock received from vendor
    SALE, // Sold to patient/dispensed
    TRANSFER_IN, // Transferred from another location
    TRANSFER_OUT, // Transferred to another location
    ADJUSTMENT_ADD, // Manual adjustment - addition
    ADJUSTMENT_REMOVE, // Manual adjustment - removal
    RETURN_TO_VENDOR, // Returned to vendor
    RETURN_FROM_PATIENT, // Returned by patient
    EXPIRED, // Removed due to expiry
    DAMAGED, // Removed due to damage
    SAMPLE, // Given as sample
    INITIAL_STOCK // Initial stock entry
}
