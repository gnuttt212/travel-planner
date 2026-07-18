package com.travelplanner.user.service;

import com.travelplanner.user.domain.UserPreference;
import com.travelplanner.user.dto.OnboardingRequest;
import com.travelplanner.user.repository.UserPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @InjectMocks
    private UserPreferenceService service;

    @Test
    void shouldSaveNewPreferencesWhenUserHasNone() {
        OnboardingRequest request = new OnboardingRequest(
                List.of("beach", "food"),
                java.math.BigDecimal.valueOf(1000.0),
                java.math.BigDecimal.valueOf(2500.0),
                "relaxed",
                "friends"
        );

        UserPreference savedPreference = UserPreference.builder()
                .userId("user-1")
                .build();

        when(userPreferenceRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(userPreferenceRepository.save(any(UserPreference.class))).thenReturn(savedPreference);

        UserPreference result = service.saveOnboardingAnswers("user-1", request);

        assertNotNull(result);
        assertEquals("user-1", result.getUserId());
        verify(userPreferenceRepository).save(any(UserPreference.class));
    }

    @Test
    void shouldUpdateExistingPreferences() {
        OnboardingRequest request = new OnboardingRequest(
                List.of("museum"),
                java.math.BigDecimal.valueOf(500.0),
                java.math.BigDecimal.valueOf(1500.0),
                "active",
                "family"
        );

        UserPreference existing = UserPreference.builder()
                .userId("user-2")
                .build();

        when(userPreferenceRepository.findByUserId("user-2")).thenReturn(Optional.of(existing));
        when(userPreferenceRepository.save(any(UserPreference.class))).thenReturn(existing);

        UserPreference result = service.saveOnboardingAnswers("user-2", request);

        assertEquals("user-2", result.getUserId());
        assertEquals(1.0, result.getTagWeights().get("museum"));
        assertEquals(java.math.BigDecimal.valueOf(500.0), result.getMinBudget());
        assertEquals(java.math.BigDecimal.valueOf(1500.0), result.getMaxBudget());
        assertEquals("active", result.getTravelStyle());
        assertEquals("family", result.getGroupType());
    }
}
