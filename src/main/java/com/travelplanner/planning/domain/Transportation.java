package com.travelplanner.planning.domain;

public enum Transportation {
    MOTORBIKE(50), CAR(150), PUBLIC_TRANSPORT(20);
    
    private final int maxRadiusKm;
    Transportation(int maxRadiusKm) { this.maxRadiusKm = maxRadiusKm; }
    public int getMaxRadiusKm() { return maxRadiusKm; }

    public static Transportation fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Transportation is required");
        }
        if ("PUBLIC".equalsIgnoreCase(value.trim())) {
            return PUBLIC_TRANSPORT;
        }
        return Transportation.valueOf(value.trim());
    }
}
