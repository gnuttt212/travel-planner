package com.travelplanner.recommendation.service;

import com.travelplanner.common.dto.RecommendationRequest;
import com.travelplanner.common.dto.RecommendationVariantResponse;
import com.travelplanner.common.dto.RecommendedActivity;
import com.travelplanner.planning.domain.Transportation;
import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.domain.TripActivity;
import com.travelplanner.planning.domain.TripDuration;
import com.travelplanner.recommendation.domain.TripPlanVariant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecommendationMapper {

    public TravelContext toTravelContext(RecommendationRequest request) {
        return TravelContext.builder()
                .startLat(request.getStartLat())
                .startLon(request.getStartLon())
                .transportation(Transportation.fromApiValue(request.getTransportationType()))
                .date(request.getDate())
                .styles(request.getStyles())
                .budgetPerPerson(request.getBudgetPerPerson())
                .duration(TripDuration.valueOf(request.getDurationType()))
                .build();
    }

    public List<RecommendationVariantResponse> toVariantResponses(List<TripPlanVariant> variants) {
        return variants.stream()
                .map(this::toVariantResponse)
                .collect(Collectors.toList());
    }

    private RecommendationVariantResponse toVariantResponse(TripPlanVariant variant) {
        return RecommendationVariantResponse.builder()
                .variantName(variant.name())
                .variantDescription(variant.description())
                .aiNarrative(variant.aiNarrative())
                .activities(variant.activities().stream().map(this::toRecommendedActivity).collect(Collectors.toList()))
                .totalCost(variant.totalCost())
                .totalDistanceKm(variant.totalDistanceKm())
                .build();
    }

    private RecommendedActivity toRecommendedActivity(TripActivity activity) {
        return RecommendedActivity.builder()
                .destinationId(activity.getDestinationId())
                .destinationName(activity.getDestinationName())
                .destinationCategory(activity.getDestinationCategory())
                .destinationLat(activity.getDestinationLat())
                .destinationLon(activity.getDestinationLon())
                .destinationRating(activity.getDestinationRating())
                .destinationImageUrl(activity.getDestinationImageUrl())
                .orderIndex(activity.getOrderIndex())
                .plannedStartTime(activity.getPlannedStartTime())
                .plannedEndTime(activity.getPlannedEndTime())
                .estimatedDurationMinutes(activity.getEstimatedDurationMinutes())
                .estimatedCost(activity.getEstimatedCost())
                .travelDistanceKm(activity.getTravelDistanceKm())
                .travelTimeMinutes(activity.getTravelTimeMinutes())
                .status(activity.getStatus() != null ? activity.getStatus().name() : null)
                .build();
    }
}
