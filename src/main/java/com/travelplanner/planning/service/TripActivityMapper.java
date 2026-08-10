package com.travelplanner.planning.service;

import com.travelplanner.common.dto.RecommendedActivity;
import com.travelplanner.planning.domain.ActivityStatus;
import com.travelplanner.planning.domain.TripActivity;
import org.springframework.stereotype.Component;

@Component
public class TripActivityMapper {
    public TripActivity toTripActivity(RecommendedActivity rec) {
        return TripActivity.builder()
                .destinationId(rec.getDestinationId())
                .destinationName(rec.getDestinationName())
                .destinationCategory(rec.getDestinationCategory())
                .destinationLat(rec.getDestinationLat())
                .destinationLon(rec.getDestinationLon())
                .destinationRating(rec.getDestinationRating())
                .destinationImageUrl(rec.getDestinationImageUrl())
                .orderIndex(rec.getOrderIndex())
                .plannedStartTime(rec.getPlannedStartTime())
                .plannedEndTime(rec.getPlannedEndTime())
                .estimatedDurationMinutes(rec.getEstimatedDurationMinutes())
                .estimatedCost(rec.getEstimatedCost())
                .travelDistanceKm(rec.getTravelDistanceKm())
                .travelTimeMinutes(rec.getTravelTimeMinutes())
                .status(rec.getStatus() != null ? ActivityStatus.valueOf(rec.getStatus()) : ActivityStatus.UPCOMING)
                .build();
    }
}
