package com.travelplanner.common.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendedActivity {
    private UUID destinationId;
    private String destinationName;
    private String destinationCategory;
    private double destinationLat;
    private double destinationLon;
    private double destinationRating;
    private String destinationImageUrl;
    private int orderIndex;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private int estimatedDurationMinutes;
    private BigDecimal estimatedCost;
    private double travelDistanceKm;
    private int travelTimeMinutes;
    private String status;
}
