package com.travelplanner.recommendation.domain;

import com.travelplanner.planning.domain.TripActivity;
import java.math.BigDecimal;
import java.util.List;

public record TripPlanVariant(
    String name,
    String description,
    List<TripActivity> activities,
    BigDecimal totalCost,
    double totalDistanceKm
) {}
