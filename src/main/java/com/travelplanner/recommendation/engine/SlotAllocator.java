package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.*;
import com.travelplanner.recommendation.domain.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SlotAllocator {
    
    private final com.travelplanner.recommendation.integration.OpenRouteServiceClient orsClient;
    
    public SlotAllocator(com.travelplanner.recommendation.integration.OpenRouteServiceClient orsClient) {
        this.orsClient = orsClient;
    }
    
    // Estimated duration per category (minutes)
    private static final Map<DestinationCategory, Integer> DURATION_MAP = Map.of(
        DestinationCategory.CAFE, 75,
        DestinationCategory.RESTAURANT, 60,
        DestinationCategory.STREET_FOOD, 45,
        DestinationCategory.PARK, 90,
        DestinationCategory.MUSEUM, 105,
        DestinationCategory.MARKET, 75,
        DestinationCategory.BAR, 90,
        DestinationCategory.TEMPLE, 60,
        DestinationCategory.SHOPPING, 90,
        DestinationCategory.ENTERTAINMENT, 120
    );
    
    public List<TripActivity> allocate(List<ScoredDestination> scored, TravelContext ctx) {
        int maxActivities = switch (ctx.duration()) {
            case HALF_DAY -> 3;
            case FULL_DAY -> 5;
            case MULTI_DAY -> 7;
        };
        
        LocalTime startTime = switch (ctx.duration()) {
            case HALF_DAY -> LocalTime.of(9, 0);
            case FULL_DAY -> LocalTime.of(9, 0);
            case MULTI_DAY -> LocalTime.of(8, 30);
        };
        
        LocalTime endTime = switch (ctx.duration()) {
            case HALF_DAY -> LocalTime.of(13, 0);
            case FULL_DAY -> LocalTime.of(20, 0);
            case MULTI_DAY -> LocalTime.of(21, 0);
        };
        
        List<TripActivity> activities = new ArrayList<>();
        LocalTime current = startTime;
        Set<UUID> usedIds = new HashSet<>();
        boolean hasMeal = false;
        
        for (ScoredDestination sd : scored) {
            if (activities.size() >= maxActivities) break;
            if (current.isAfter(endTime)) break;
            if (usedIds.contains(sd.destination().getId())) continue;
            
            Destination dest = sd.destination();
            int durationMin = DURATION_MAP.getOrDefault(dest.getCategory(), 60);
            
            // Ensure meal slot (lunch 11:30-13:00)
            boolean isMealTime = current.isAfter(LocalTime.of(11, 30)) && current.isBefore(LocalTime.of(13, 30));
            boolean isRestaurant = dest.getCategory() == DestinationCategory.RESTAURANT || dest.getCategory() == DestinationCategory.STREET_FOOD;
            
            if (isMealTime && !hasMeal && !isRestaurant) continue; // skip non-food during meal time
            if (isRestaurant) hasMeal = true;
            
            // Calculate travel time from previous activity
            int travelMinutes = 15; // default 15 min travel buffer
            double travelKm = 0;
            if (!activities.isEmpty()) {
                TripActivity prev = activities.get(activities.size() - 1);
                try {
                    var info = orsClient.getRoute(prev.getDestinationLat(), prev.getDestinationLon(), dest.getLatitude(), dest.getLongitude(), ctx.transportation());
                    travelKm = info.distanceKm();
                    travelMinutes = info.durationMinutes();
                } catch (Exception e) {
                    // Fallback to Haversine
                    travelKm = haversineKm(prev.getDestinationLat(), prev.getDestinationLon(), dest.getLatitude(), dest.getLongitude());
                    travelMinutes = estimateTravelMinutes(travelKm, ctx.transportation());
                }
            }
            
            LocalTime actStart = current.plusMinutes(activities.isEmpty() ? 0 : travelMinutes);
            LocalTime actEnd = actStart.plusMinutes(durationMin);
            
            if (actEnd.isAfter(endTime)) break;
            
            TripActivity activity = TripActivity.builder()
                .destinationId(dest.getId())
                .orderIndex(activities.size())
                .plannedStartTime(actStart)
                .plannedEndTime(actEnd)
                .estimatedDurationMinutes(durationMin)
                .estimatedCost(dest.getAvgCostPerPerson())
                .destinationName(dest.getName())
                .destinationCategory(dest.getCategory() != null ? dest.getCategory().name() : "OTHER")
                .destinationLat(dest.getLatitude())
                .destinationLon(dest.getLongitude())
                .destinationRating(dest.getAvgRating())
                .destinationImageUrl(dest.getImageUrl())
                .travelDistanceKm(travelKm)
                .travelTimeMinutes(travelMinutes)
                .status(ActivityStatus.UPCOMING)
                .build();
            
            activities.add(activity);
            usedIds.add(dest.getId());
            current = actEnd;
        }
        
        return activities;
    }
    
    private int estimateTravelMinutes(double km, Transportation transport) {
        double speedKmh = switch (transport) {
            case MOTORBIKE -> 25;
            case CAR -> 30;
            case PUBLIC_TRANSPORT -> 15;
        };
        return Math.max(5, (int) Math.ceil((km / speedKmh) * 60));
    }
    
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
