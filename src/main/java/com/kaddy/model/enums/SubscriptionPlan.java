package com.kaddy.model.enums;

public enum SubscriptionPlan {
    TRIAL("Trial", 0, 10, 100, false, false), BASIC("Basic", 99, 25, 500, false, true), PROFESSIONAL("Professional",
            299, 100, 5000, true, true), ENTERPRISE("Enterprise", 999, -1, -1, true, true);

    private final String displayName;
    private final int monthlyPriceUsd;
    private final int maxUsers;
    private final int maxPatients;
    private final boolean aiEnabled;
    private final boolean fhirEnabled;

    SubscriptionPlan(String displayName, int monthlyPriceUsd, int maxUsers, int maxPatients, boolean aiEnabled,
            boolean fhirEnabled) {
        this.displayName = displayName;
        this.monthlyPriceUsd = monthlyPriceUsd;
        this.maxUsers = maxUsers;
        this.maxPatients = maxPatients;
        this.aiEnabled = aiEnabled;
        this.fhirEnabled = fhirEnabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMonthlyPriceUsd() {
        return monthlyPriceUsd;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public int getMaxPatients() {
        return maxPatients;
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public boolean isFhirEnabled() {
        return fhirEnabled;
    }
}
