package com.kaddy.model.enums;

public enum EmergencyRoomStatus {
    AVAILABLE, // Room is vacant and ready for patients
    OCCUPIED, // Room is currently occupied by a patient
    CLEANING, // Room is being cleaned/sanitized
    MAINTENANCE, // Room is under maintenance
    RESERVED // Room is reserved for incoming patient
}
