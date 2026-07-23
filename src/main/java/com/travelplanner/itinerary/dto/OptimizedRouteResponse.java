package com.travelplanner.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizedRouteResponse {
    private List<WaypointDto> optimizedOrder;
    private double totalDurationMinutes;
    private double totalDistanceKm;
    private double carbonEmissionKg;
}
