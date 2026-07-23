package com.travelplanner.itinerary.dto;

import jakarta.validation.constraints.NotBlank;

public record ReplanRequest(
        @NotBlank(message = "Itinerary ID is required")
        String itineraryId,
        
        @NotBlank(message = "Original itinerary is required")
        String originalItinerary,
        
        @NotBlank(message = "Event type is required")
        String eventType,
        
        @NotBlank(message = "Event details are required")
        String eventDetails,
        
        Double latitude,
        
        Double longitude
) {
}
