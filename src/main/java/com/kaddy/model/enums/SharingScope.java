package com.kaddy.model.enums;

public enum SharingScope {
    ALL_RECORDS("Share all medical records"), MEDICAL_HISTORY("Share medical history only"), LAB_RESULTS(
            "Share lab test results only"), PRESCRIPTIONS("Share prescriptions only"), DIAGNOSES(
                    "Share diagnoses only"), IMAGING("Share imaging/radiology reports only"), VITAL_SIGNS(
                            "Share vital signs only"), SPECIFIC_RECORDS(
                                    "Share specific selected records"), SUMMARY_ONLY("Share summary/overview only");

    private final String description;

    SharingScope(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
