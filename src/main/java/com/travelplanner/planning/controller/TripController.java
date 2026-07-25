package com.travelplanner.planning.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.planning.domain.*;
import com.travelplanner.planning.dto.*;
import com.travelplanner.planning.service.TripService;
import com.travelplanner.recommendation.domain.TripPlanVariant;
import com.travelplanner.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TripController {
    
    private final RecommendationService recommendationService;
    private final TripService tripService;
    
    @PostMapping("/planning/recommend")
    public ApiResponse<List<TripPlanResponse>> recommend(@RequestBody TripRequest request) {
        TravelContext ctx = request.toTravelContext();
        List<TripPlanVariant> variants = recommendationService.recommend(ctx, request.city());
        
        List<TripPlanResponse> response = variants.stream()
            .map(v -> new TripPlanResponse(v.name(), v.description(), v.activities(), v.totalCost(), v.totalDistanceKm()))
            .collect(Collectors.toList());
        
        return ApiResponse.success(response);
    }
    
    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TripResponse> createTrip(
            Authentication authentication,
            @RequestBody TripRequest request
    ) {
        String userId = authentication.getName();
        Trip trip = tripService.createFromPlan(userId, request);
        return ApiResponse.success(toResponse(trip));
    }
    
    @GetMapping("/trips")
    public ApiResponse<List<TripResponse>> getMyTrips(Authentication authentication) {
        String userId = authentication.getName();
        List<Trip> trips = tripService.getByOwner(userId);
        return ApiResponse.success(trips.stream().map(this::toResponse).collect(Collectors.toList()));
    }
    
    @GetMapping("/trips/{id}")
    public ApiResponse<TripResponse> getTrip(@PathVariable UUID id) {
        Trip trip = tripService.getById(id);
        return ApiResponse.success(toResponse(trip));
    }
    
    @DeleteMapping("/trips/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable UUID id) {
        tripService.delete(id);
    }
    
    private TripResponse toResponse(Trip trip) {
        return new TripResponse(
            trip.getId(), trip.getTitle(), trip.getStatus(), trip.getTripDate(),
            trip.getPurpose(), trip.getGroupSize(), trip.getActivities(), trip.getCreatedAt()
        );
    }
}
