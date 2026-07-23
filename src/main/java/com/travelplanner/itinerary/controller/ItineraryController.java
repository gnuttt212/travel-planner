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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.travelplanner.itinerary.domain.Itinerary;
import com.travelplanner.itinerary.domain.ItineraryReadModel;
import com.travelplanner.itinerary.dto.ReplanRequest;
import com.travelplanner.itinerary.repository.ItineraryRepository;
import com.travelplanner.itinerary.repository.ItineraryReadModelRepository;
import com.travelplanner.itinerary.service.ItineraryService;
import com.travelplanner.common.exception.ResourceNotFoundException;

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
    private final ItineraryService itineraryService;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryReadModelRepository itineraryReadModelRepository;

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
        
        // 5. Save and Publish Event
        Itinerary itinerary = Itinerary.builder()
                .ownerId(userId)
                .title("Generated Trip to " + primary.name())
                .content(detailedItinerary)
                .build();
        Itinerary saved = itineraryService.saveAndPublish(itinerary, "CREATED");
        
        return ApiResponse.success(saved.getContent());
    }

    @PostMapping("/replan")
    public ApiResponse<String> replanItinerary(
            Authentication authentication,
            @Valid @RequestBody ReplanRequest request
    ) {
        String weatherContext = null;
        if (request.latitude() != null && request.longitude() != null) {
            weatherContext = weatherService.getWeatherForecast(request.latitude(), request.longitude());
        }

        String replannedItinerary = geminiService.replanItinerary(
                request.originalItinerary(),
                request.eventType(),
                request.eventDetails(),
                weatherContext
        );

        Itinerary itinerary = itineraryRepository.findById(request.itineraryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Itinerary", request.itineraryId()));
        itinerary.setContent(replannedItinerary);
        Itinerary saved = itineraryService.saveAndPublish(itinerary, "UPDATED");

        return ApiResponse.success(saved.getContent());
    }

    @GetMapping("/{id}/view")
    public ApiResponse<String> viewItinerary(@PathVariable String id) {
        // Read directly from CQRS Read Model for high performance
        ItineraryReadModel readModel = itineraryReadModelRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ItineraryReadModel", id));
        return ApiResponse.success(readModel.getDenormalizedData());
    }
}
