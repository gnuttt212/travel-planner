package com.travelplanner.user.dto;

import java.math.BigDecimal;
import java.util.List;

public record OnboardingRequest(
        List<String> preferredTags,
        BigDecimal minBudget,
        BigDecimal maxBudget,
        String travelStyle,
        String groupType
) {}
