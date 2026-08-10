package com.travelplanner.planning.controller;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.planning.domain.*;
import com.travelplanner.planning.dto.*;
import com.travelplanner.planning.security.TripSecurity;
import com.travelplanner.planning.service.TripService;
import com.travelplanner.planning.service.TripActivityMapper;
import com.travelplanner.common.dto.RecommendationRequest;
import com.travelplanner.common.dto.RecommendationVariantResponse;
import com.travelplanner.planning.port.RecommendationPort;
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
    
    private final RecommendationPort recommendationPort;
    private final TripService tripService;
    private final TripSecurity tripSecurity;
    private final TripActivityMapper activityMapper;
    
    @PostMapping("/planning/recommend")
    public ApiResponse<List<TripPlanResponse>> recommend(@RequestBody TripRequest request) {
        RecommendationRequest recRequest = RecommendationRequest.builder()
                .city(request.city())
                .startLat(request.startLat())
                .startLon(request.startLon())
                .transportationType(request.transportation())
                .date(request.tripDate())
                .styles(request.styles())
                .budgetPerPerson(request.budgetPerPerson())
                .durationType(request.duration())
                .build();
        
        List<RecommendationVariantResponse> variants = recommendationPort.recommend(recRequest);
        
        List<TripPlanResponse> response = variants.stream()
            .map(v -> new TripPlanResponse(v.getVariantName(), v.getVariantDescription(), 
                v.getActivities().stream().map(activityMapper::toTripActivity).collect(Collectors.toList()), 
                v.getTotalCost(), v.getTotalDistanceKm()))
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

    @PatchMapping("/trips/{tripId}/activities/reorder")
    @PreAuthorize("@tripSecurity.isOwner(#tripId, authentication.name) or hasRole('ADMIN')")
    public ApiResponse<TripResponse> reorderActivities(
            @PathVariable UUID tripId,
            @RequestBody List<UUID> activityIdsInNewOrder
    ) {
        Trip updated = tripService.reorderActivities(tripId, activityIdsInNewOrder);
        return ApiResponse.success(toResponse(updated));
    }
    
    private TripResponse toResponse(Trip trip) {
        return new TripResponse(
            trip.getId(), trip.getTitle(), trip.getStatus(), trip.getTripDate(),
            trip.getPurpose(), trip.getGroupSize(), trip.getActivities(), trip.getCreatedAt()
        );
    }
}
