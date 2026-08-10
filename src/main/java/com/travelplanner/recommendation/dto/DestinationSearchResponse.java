package com.travelplanner.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationSearchResponse {
    private DestinationSearchFilters filters;
    private List<com.travelplanner.recommendation.dto.DestinationDto> destinations;
}
