package com.travelplanner.itinerary.event;

import lombok.Getter;
import java.util.UUID;

@Getter
public class WeatherChangeDetectedEvent {
    private final UUID tripId;
    private final String detail;

    public WeatherChangeDetectedEvent(UUID tripId, String detail) {
        this.tripId = tripId;
        this.detail = detail;
    }
}
