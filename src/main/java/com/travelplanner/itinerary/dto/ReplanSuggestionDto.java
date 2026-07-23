package com.travelplanner.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplanSuggestionDto {
    private String id;
    private String itineraryId;
    private String triggerReason;
    private String suggestedChanges;
    private String status;
    private Instant createdAt;
}
