package com.travelplanner.itinerary.dto;

import java.math.BigDecimal;
import java.util.List;

public record DestinationResponse(
        String id,
        String name,
        String country,
        String description,
        List<String> tags,
        List<Integer> bestMonths,
        BigDecimal avgCostPerDay,
        Double popularityScore,
        Double latitude,
        Double longitude
) {
}
