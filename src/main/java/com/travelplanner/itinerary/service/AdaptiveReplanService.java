package com.travelplanner.itinerary.service;

import com.travelplanner.itinerary.domain.ReplanSuggestion;
import java.util.UUID;

public interface AdaptiveReplanService {
    void triggerReplan(UUID tripId, ReplanSuggestion.TriggerReason reason, String triggerDetail);
    void resolveSuggestion(UUID suggestionId, boolean accepted);
}
