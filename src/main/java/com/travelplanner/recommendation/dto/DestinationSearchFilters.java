package com.travelplanner.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DestinationSearchFilters {
    private String category;
    private List<String> tags;
    private String city;
    private BigDecimal maxBudget;
    private Double minRating;
    private Double proximityKm;
    private Boolean indoor;
    private Boolean openNow;
    private String sortBy;
}
