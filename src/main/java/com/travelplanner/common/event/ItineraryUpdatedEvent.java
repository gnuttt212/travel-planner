package com.travelplanner.common.event;

public record ItineraryUpdatedEvent(
        String itineraryId,
        String action,
        String content
) {
}
