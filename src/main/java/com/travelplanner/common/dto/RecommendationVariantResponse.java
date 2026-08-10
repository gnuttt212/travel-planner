package com.travelplanner.common.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationVariantResponse {
    private String variantName;
    private String variantDescription;
    private String aiNarrative;
    private List<RecommendedActivity> activities;
    private BigDecimal totalCost;
    private double totalDistanceKm;
}
