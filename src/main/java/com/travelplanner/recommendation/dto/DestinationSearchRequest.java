package com.travelplanner.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationSearchRequest {
    private String userQuery;
    private Double userLat;
    private Double userLon;
}
