package com.example.practicalfive;

/** Shared validation for both Activities; this class has no Android dependencies. */
public final class TextProcessor {
    private static final String[] SUPPORTED = {"Beijing", "Shanghai", "Chongqing", "Tianjin"};

    /** Returns the canonical name, or null for missing/unsupported input. */
    public String processMunicipality(String municipality) {
        if (isBlank(municipality)) {
            return null;
        }
        String candidate = municipality.trim();
        for (String name : SUPPORTED) {
            if (name.equalsIgnoreCase(candidate)) {
                return name;
            }
        }
        return null;
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
