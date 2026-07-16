package com.travelplanner.itinerary.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.itinerary.dto.DestinationResponse;
import com.travelplanner.itinerary.service.DestinationService;
import com.travelplanner.itinerary.service.GeminiService;
import com.travelplanner.itinerary.service.WeatherService;
import com.travelplanner.user.domain.UserPreference;
import com.travelplanner.user.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final DestinationService destinationService;
    private final GeminiService geminiService;
    private final WeatherService weatherService;
    private final UserPreferenceRepository userPreferenceRepository;

    @GetMapping("/generate")
    public ApiResponse<String> generateItinerary(
            Authentication authentication,
            @RequestParam BigDecimal maxBudgetPerDay,
            @RequestParam Integer travelMonth
    ) {
        String userId = authentication.getName();
        
        // 1. Get Top Destinations (Vector Search + Bayesian Rating + Hard Filter)
        List<DestinationResponse> topDestinations = destinationService.recommend(userId, maxBudgetPerDay, travelMonth);
        
        if (topDestinations.isEmpty()) {
            return ApiResponse.success("No matching destinations found for your criteria.");
        }
        
        // 2. Fetch User Profile for prompt context
        UserPreference userPreference = userPreferenceRepository.findByUserId(userId).orElse(null);
        
        // 3. Fetch weather for primary destination
        String weatherContext = null;
        DestinationResponse primary = topDestinations.get(0);
        if (primary.latitude() != null && primary.longitude() != null) {
            weatherContext = weatherService.getWeatherForecast(primary.latitude(), primary.longitude());
        }
        
        // 4. Generate detailed itinerary via Gemini RAG (weather-aware)
        String detailedItinerary = geminiService.generateDetailedItinerary(topDestinations, userPreference, weatherContext);
        
        return ApiResponse.success(detailedItinerary);
    }
}
