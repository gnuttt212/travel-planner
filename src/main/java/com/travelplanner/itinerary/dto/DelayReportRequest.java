package com.travelplanner.itinerary.dto;

import jakarta.validation.constraints.NotNull;

public record DelayReportRequest(
    @NotNull(message = "Delay duration in minutes is required")
    Integer delayMinutes,
    String reason
) {}
