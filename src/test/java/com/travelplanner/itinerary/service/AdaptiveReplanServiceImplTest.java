package com.travelplanner.itinerary.service;

import com.travelplanner.itinerary.domain.ReplanSuggestion;
import com.travelplanner.itinerary.repository.ReplanSuggestionRepository;
import com.travelplanner.itinerary.service.impl.AdaptiveReplanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdaptiveReplanServiceImplTest {

    @Mock
    private ReplanSuggestionRepository suggestionRepository;

    @InjectMocks
    private AdaptiveReplanServiceImpl adaptiveReplanService;

    private UUID tripId;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
    }

    @Test
    void triggerReplan_ShouldSaveSuggestion() {
        adaptiveReplanService.triggerReplan(tripId, ReplanSuggestion.TriggerReason.USER_DELAY, "Trễ 30 phút");
        
        verify(suggestionRepository, times(1)).save(any(ReplanSuggestion.class));
    }

    @Test
    void resolveSuggestion_WhenAccepted_ShouldUpdateStatusToAccepted() {
        UUID suggestionId = UUID.randomUUID();
        ReplanSuggestion suggestion = new ReplanSuggestion();
        suggestion.setId(suggestionId.toString());
        suggestion.setStatus("PENDING");

        when(suggestionRepository.findById(suggestionId.toString())).thenReturn(Optional.of(suggestion));

        adaptiveReplanService.resolveSuggestion(suggestionId, true);

        verify(suggestionRepository, times(1)).save(suggestion);
        assert suggestion.getStatus().equals("ACCEPTED");
    }
}
