package com.travelplanner.planning.port;

import com.travelplanner.common.dto.RecommendationRequest;
import com.travelplanner.common.dto.RecommendationVariantResponse;
import java.util.List;

public interface RecommendationPort {
    List<RecommendationVariantResponse> recommend(RecommendationRequest request);
}
