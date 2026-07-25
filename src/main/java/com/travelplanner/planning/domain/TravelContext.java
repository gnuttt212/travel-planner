package com.travelplanner.planning.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record TravelContext(
    TripPurpose purpose,
    LocalDate date,
    TripDuration duration,
    String groupType,
    int groupSize,
    BigDecimal budgetPerPerson,
    List<String> styles,
    double startLat,
    double startLon,
    Transportation transportation
) {}
