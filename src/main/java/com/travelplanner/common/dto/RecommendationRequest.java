package com.travelplanner.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationRequest {
    private String city;
    private double startLat;
    private double startLon;
    private String transportationType;
    private double transportationMaxRadius;
    private LocalDate date;
    private List<String> styles;
    private BigDecimal budgetPerPerson;
    private String durationType;
}
