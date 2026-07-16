package com.travelplanner.itinerary.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.itinerary.dto.DestinationResponse;
import com.travelplanner.itinerary.service.CollaborativeFilteringService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
public class CollaborativeController {

    private final CollaborativeFilteringService collaborativeService;

    @GetMapping("/collaborative-recommend")
    public ApiResponse<List<DestinationResponse>> recommendBasedOnSimilarUsers(Authentication authentication) {
        String userId = authentication.getName();
        return ApiResponse.success(collaborativeService.getCollaborativeRecommendations(userId));
    }
}
