package com.travelplanner.recommendation.service;

import com.travelplanner.common.dto.RecommendationRequest;
import com.travelplanner.common.dto.RecommendationVariantResponse;
import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.port.RecommendationPort;
import com.travelplanner.recommendation.domain.TripPlanVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationPortImpl implements RecommendationPort {

    private final RecommendationService recommendationService;
    private final RecommendationMapper mapper;

    @Override
    public List<RecommendationVariantResponse> recommend(RecommendationRequest request) {
        TravelContext ctx = mapper.toTravelContext(request);
        List<TripPlanVariant> variants = recommendationService.recommend(ctx, request.getCity());
        return mapper.toVariantResponses(variants);
    }
}
