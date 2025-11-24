package com.kaddy.model.enums;

public enum BillingStatus {
    DRAFT, // Invoice being prepared
    PENDING, // Awaiting payment
    PARTIAL_PAID, // Partially paid
    PAID, // Fully paid
    OVERDUE, // Payment overdue
    CANCELLED, // Invoice cancelled
    REFUNDED // Payment refunded
}
