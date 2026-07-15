package com.travelplanner.user.service;

import com.travelplanner.user.domain.UserPreference;
import com.travelplanner.user.dto.OnboardingRequest;
import com.travelplanner.user.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreference saveOnboardingAnswers(String userId, OnboardingRequest request) {
        // Build tag weights
        Map<String, Double> tagWeights = new HashMap<>();
        if (request.preferredTags() != null) {
            for (String tag : request.preferredTags()) {
                tagWeights.put(tag, 1.0); // Default weight, can be updated later by user actions
            }
        }

        // We will initially leave preferenceVector as null.
        // It will be updated by a background process or script based on these preferences.

        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreference.builder().userId(userId).build());

        preference.setTagWeights(tagWeights);
        preference.setMinBudget(request.minBudget());
        preference.setMaxBudget(request.maxBudget());
        preference.setTravelStyle(request.travelStyle());
        preference.setGroupType(request.groupType());
        
        return userPreferenceRepository.save(preference);
    }
}
