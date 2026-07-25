package com.travelplanner.identity.service;

import com.travelplanner.identity.domain.UserPreference;
import com.travelplanner.identity.dto.OnboardingRequest;
import com.travelplanner.identity.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreference saveOnboardingAnswers(String userId, OnboardingRequest request) {
        Map<String, Double> tagWeights = new HashMap<>();
        if (request.preferredTags() != null) {
            for (String tag : request.preferredTags()) {
                tagWeights.put(tag, 1.0);
            }
        }

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
