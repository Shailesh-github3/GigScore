package com.org.gigscore.dto;

public enum ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW;

    public static ConfidenceLevel fromVolume(int gigs, int activeDays) {
        if (gigs >= 20 && activeDays >= 10) {
            return HIGH;
        }
        if (gigs >= 5 && activeDays >= 3) {
            return MEDIUM;
        }
        return LOW;
    }
}