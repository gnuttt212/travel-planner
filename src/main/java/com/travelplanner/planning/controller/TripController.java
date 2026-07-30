package com.travelplanner.planning.controller;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.planning.domain.*;
import com.travelplanner.planning.dto.*;
import com.travelplanner.planning.security.TripSecurity;
import com.travelplanner.planning.service.TripService;
import com.travelplanner.recommendation.domain.TripPlanVariant;
import com.travelplanner.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TripController {
    
    private final RecommendationService recommendationService;
    private final TripService tripService;
    private final TripSecurity tripSecurity;
    
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
    public ApiResponse<TripResponse> getTrip(@PathVariable UUID id, Authentication authentication) {
        // Ownership/visibility check: owner, bạn bè (FRIENDS_ONLY), public, hoặc ADMIN
        if (!tripSecurity.canView(id, authentication)) {
            throw AccessDeniedException.of("trip");
        }
        Trip trip = tripService.getById(id);
        return ApiResponse.success(toResponse(trip));
    }
    
    @DeleteMapping("/trips/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@tripSecurity.isOwner(#id, authentication.name) or hasRole('ADMIN')")
    public void deleteTrip(@PathVariable UUID id) {
        tripService.delete(id);
    }

    @GetMapping("/users/{email}/trips")
    public ApiResponse<List<TripResponse>> getTripsByOwner(
            @PathVariable String email,
            Authentication authentication
    ) {
        // Trả về trip theo visibility: owner thấy tất cả, bạn bè thấy PUBLIC + FRIENDS_ONLY,
        // người lạ chỉ thấy PUBLIC, ADMIN thấy tất cả
        String viewerEmail = authentication.getName();
        List<Trip> visibleTrips = tripSecurity.getVisibleTrips(email, viewerEmail, authentication);
        List<TripResponse> resp = visibleTrips.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(resp);
    }
    
    private TripResponse toResponse(Trip trip) {
        return new TripResponse(
            trip.getId(), trip.getTitle(), trip.getStatus(), trip.getTripDate(),
            trip.getPurpose(), trip.getGroupSize(), trip.getActivities(), trip.getCreatedAt()
        );
    }
}
