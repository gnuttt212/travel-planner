package com.travelplanner.planning.domain;

public enum Transportation {
    MOTORBIKE(50), CAR(150), PUBLIC_TRANSPORT(20);
    
    private final int maxRadiusKm;
    Transportation(int maxRadiusKm) { this.maxRadiusKm = maxRadiusKm; }
    public int getMaxRadiusKm() { return maxRadiusKm; }
}
