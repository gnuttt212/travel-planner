package com.travelplanner.identity.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.identity.domain.UserPreference;
import com.travelplanner.identity.dto.OnboardingRequest;
import com.travelplanner.identity.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final UserPreferenceService userPreferenceService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserPreference>> submitOnboarding(
            Authentication authentication,
            @RequestBody OnboardingRequest request
    ) {
        String userId = authentication.getName();
        UserPreference saved = userPreferenceService.saveOnboardingAnswers(userId, request);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }
}
