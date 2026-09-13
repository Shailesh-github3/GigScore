package com.org.gigscore.util;

import java.util.Arrays;
import java.util.List;

import com.org.gigscore.exception.BadRequestException;

public enum Platform {
    SWIGGY("Swiggy"),
    ZOMATO("Zomato"),
    UBER("Uber"),
    RAPIDO("Rapido"),
    UPWORK("Upwork"),
    FIVERR("Fiverr"),
    LYFT("Lyft"),
    DOORDASH("DoorDash"),
    TASK_RABBIT("TaskRabbit"),
    FREELANCER("Freelancer"),
    OTHER("Other");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static List<String> allowedValues() {
        return Arrays.stream(values()).map(Platform::displayName).toList();
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Platform is required.");
        }
        String key = key(raw);
        for (Platform platform : values()) {
            if (key(platform.displayName).equals(key) || key(platform.name()).equals(key)) {
                return platform.displayName;
            }
        }
        throw new BadRequestException("Unknown platform '" + raw.trim()
                + "'. Allowed values: " + String.join(", ", allowedValues()) + ".");
    }

    private static String key(String value) {
        return value.replaceAll("[^a-zA-Z]", "").toLowerCase();
    }
}