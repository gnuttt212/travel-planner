package com.travelplanner.planning.dto;

import com.travelplanner.planning.domain.*;
import java.time.*;
import java.util.*;

public record TripResponse(
    UUID id,
    String title,
    TripStatus status,
    LocalDate tripDate,
    TripPurpose purpose,
    int groupSize,
    List<TripActivity> activities,
    Instant createdAt
) {}
