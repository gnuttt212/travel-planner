package com.travelplanner.itinerary.event;

import lombok.Getter;
import java.util.UUID;

@Getter
public class UserDelayReportedEvent {
    private final UUID tripId;
    private final int delayMinutes;

    public UserDelayReportedEvent(UUID tripId, int delayMinutes) {
        this.tripId = tripId;
        this.delayMinutes = delayMinutes;
    }
}
