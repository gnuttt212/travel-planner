package com.travelplanner.planning.dto;

import com.travelplanner.planning.domain.TripActivity;
import java.math.BigDecimal;
import java.util.List;

public record TripPlanResponse(
    String variantName,
    String variantDescription,
    List<TripActivity> activities,
    BigDecimal totalCost,
    double totalDistanceKm
) {}
