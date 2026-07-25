package com.travelplanner.planning.service;

import com.travelplanner.common.exception.ResourceNotFoundException;
import com.travelplanner.planning.domain.*;
import com.travelplanner.planning.dto.TripRequest;
import com.travelplanner.planning.repository.TripRepository;
import com.travelplanner.recommendation.domain.TripPlanVariant;
import com.travelplanner.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TripService {
    
    private final TripRepository tripRepository;
    private final RecommendationService recommendationService;
    
    @Transactional
    public Trip createFromPlan(String userId, TripRequest request) {
        TravelContext ctx = request.toTravelContext();
        
        // Get first plan variant (balanced)
        List<TripPlanVariant> variants = recommendationService.recommend(ctx, request.city());
        List<TripActivity> activities = variants.isEmpty() ? List.of() : variants.get(0).activities();
        
        Trip trip = Trip.builder()
            .ownerId(userId)
            .title("Chuyến đi " + request.city())
            .status(TripStatus.PLANNED)
            .tripDate(request.tripDate())
            .duration(TripDuration.valueOf(request.duration()))
            .purpose(TripPurpose.valueOf(request.purpose()))
            .groupSize(request.groupSize())
            .startLat(request.startLat())
            .startLon(request.startLon())
            .transportation(Transportation.valueOf(request.transportation()))
            .activities(new ArrayList<>(activities))
            .createdAt(Instant.now())
            .build();
        
        return tripRepository.save(trip);
    }
    
    public List<Trip> getByOwner(String ownerId) {
        return tripRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }
    
    public Trip getById(UUID id) {
        return tripRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Trip", id.toString()));
    }
    
    @Transactional
    public void delete(UUID id) {
        tripRepository.deleteById(id);
    }
}
