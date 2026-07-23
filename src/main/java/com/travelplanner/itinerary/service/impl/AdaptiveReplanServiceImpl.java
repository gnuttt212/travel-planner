package com.travelplanner.itinerary.service.impl;

import com.travelplanner.itinerary.domain.ReplanSuggestion;
import com.travelplanner.itinerary.repository.ReplanSuggestionRepository;
import com.travelplanner.itinerary.service.AdaptiveReplanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveReplanServiceImpl implements AdaptiveReplanService {

    private final ReplanSuggestionRepository suggestionRepository;

    @Override
    @Transactional
    public void triggerReplan(UUID tripId, ReplanSuggestion.TriggerReason reason, String triggerDetail) {
        log.info("Triggering replan for trip {}, reason: {}, detail: {}", tripId, reason, triggerDetail);
        
        ReplanSuggestion suggestion = ReplanSuggestion.builder()
                .itineraryId(tripId.toString())
                .triggerReason(reason)
                .suggestedChanges("Suggested changes for: " + triggerDetail)
                .status("PENDING")
                .build();
                
        suggestionRepository.save(suggestion);
    }

    @Override
    @Transactional
    public void resolveSuggestion(UUID suggestionId, boolean accepted) {
        suggestionRepository.findById(suggestionId.toString()).ifPresent(suggestion -> {
            suggestion.setStatus(accepted ? "ACCEPTED" : "REJECTED");
            suggestionRepository.save(suggestion);
            log.info("Resolved replan suggestion: {}, accepted: {}", suggestionId, accepted);
        });
    }
}
