package com.travelplanner.interaction.domain;

public enum InteractionType {
    VIEW(1.0),
    SAVE(3.0),
    SELECT(5.0);

    private final double weight;

    InteractionType(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
