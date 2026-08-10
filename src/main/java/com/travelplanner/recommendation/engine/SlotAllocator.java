package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.*;
import com.travelplanner.recommendation.domain.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;

@Component
public class SlotAllocator {
    
    private final com.travelplanner.recommendation.integration.OpenRouteServiceClient orsClient;
    private final com.travelplanner.recommendation.integration.OsrmRoutingClient osrmClient;
    
    public SlotAllocator(com.travelplanner.recommendation.integration.OpenRouteServiceClient orsClient,
                         com.travelplanner.recommendation.integration.OsrmRoutingClient osrmClient) {
        this.orsClient = orsClient;
        this.osrmClient = osrmClient;
    }
    
    // Estimated duration per category (minutes)
    private static final Map<DestinationCategory, Integer> DURATION_MAP = Map.ofEntries(
        Map.entry(DestinationCategory.CAFE, 75),
        Map.entry(DestinationCategory.RESTAURANT, 60),
        Map.entry(DestinationCategory.STREET_FOOD, 45),
        Map.entry(DestinationCategory.PARK, 90),
        Map.entry(DestinationCategory.BEACH, 120),
        Map.entry(DestinationCategory.GARDEN, 90),
        Map.entry(DestinationCategory.VIEWPOINT, 45),
        Map.entry(DestinationCategory.MUSEUM, 105),
        Map.entry(DestinationCategory.MARKET, 75),
        Map.entry(DestinationCategory.BAR, 90),
        Map.entry(DestinationCategory.TEMPLE, 60),
        Map.entry(DestinationCategory.SHOPPING, 90),
        Map.entry(DestinationCategory.ENTERTAINMENT, 120)
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
        BigDecimal remainingBudget = ctx.budgetPerPerson();
        
        for (ScoredDestination sd : scored) {
            if (activities.size() >= maxActivities) break;
            if (current.isAfter(endTime)) break;
            if (usedIds.contains(sd.destination().getId())) continue;
            
            Destination dest = sd.destination();
            int durationMin = estimateDurationMinutes(dest);
            BigDecimal estimatedCost = safeCost(dest.getAvgCostPerPerson());
            if (!fitsBudget(estimatedCost, remainingBudget)) continue;
            
            // Ensure meal slot (lunch 11:30-13:00)
            boolean isMealTime = current.isAfter(LocalTime.of(11, 30)) && current.isBefore(LocalTime.of(13, 30));
            boolean isRestaurant = dest.getCategory() == DestinationCategory.RESTAURANT || dest.getCategory() == DestinationCategory.STREET_FOOD;
            
            if (isMealTime && !hasMeal && !isRestaurant) continue; // skip non-food during meal time
            if (isRestaurant) hasMeal = true;
            
            // Calculate travel time from previous activity
            int travelMinutes = 0;
            double travelKm = 0;
            if (!activities.isEmpty()) {
                TripActivity prev = activities.get(activities.size() - 1);
                var info = routeBetween(
                    prev.getDestinationLat(), prev.getDestinationLon(),
                    dest.getLatitude(), dest.getLongitude(),
                    ctx.transportation());
                travelKm = info.distanceKm();
                travelMinutes = info.durationMinutes();
            } else if (hasUsableStart(ctx)) {
                var info = routeBetween(
                    ctx.startLat(), ctx.startLon(),
                    dest.getLatitude(), dest.getLongitude(),
                    ctx.transportation());
                travelKm = info.distanceKm();
                travelMinutes = info.durationMinutes();
            }
            
            LocalTime actStart = current.plusMinutes(travelMinutes);
            LocalTime actEnd = actStart.plusMinutes(durationMin);
            
            if (actEnd.isAfter(endTime)) continue;
            
            TripActivity activity = TripActivity.builder()
                .destinationId(dest.getId())
                .orderIndex(activities.size())
                .plannedStartTime(actStart)
                .plannedEndTime(actEnd)
                .estimatedDurationMinutes(durationMin)
                .estimatedCost(estimatedCost)
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
            if (remainingBudget != null) {
                remainingBudget = remainingBudget.subtract(estimatedCost);
            }
        }
        
        return activities;
    }

    private RoutingInfo routeBetween(double fromLat, double fromLon, double toLat, double toLon, Transportation transportation) {
        try {
            var info = orsClient.getRoute(fromLat, fromLon, toLat, toLon, transportation);
            return new RoutingInfo(info.distanceKm(), info.durationMinutes());
        } catch (Exception e) {
            try {
                var info = osrmClient.route(fromLat, fromLon, toLat, toLon);
                return new RoutingInfo(info.distanceKm(), info.durationMinutes());
            } catch (Exception ex) {
                double travelKm = haversineKm(fromLat, fromLon, toLat, toLon);
                return new RoutingInfo(travelKm, estimateTravelMinutes(travelKm, transportation));
            }
        }
    }

    private int estimateDurationMinutes(Destination dest) {
        int baseDuration = DURATION_MAP.getOrDefault(dest.getCategory(), 60);
        BigDecimal cost = safeCost(dest.getAvgCostPerPerson());
        if (dest.getCategory() == DestinationCategory.ENTERTAINMENT
                && cost.compareTo(BigDecimal.valueOf(400000)) >= 0) {
            return Math.max(baseDuration, 240);
        }
        if (dest.getCategory() == DestinationCategory.PARK
                && dest.getTagList().stream().anyMatch(tag -> tag.equalsIgnoreCase("boat"))) {
            return Math.max(baseDuration, 180);
        }
        return baseDuration;
    }

    private boolean fitsBudget(BigDecimal estimatedCost, BigDecimal remainingBudget) {
        return remainingBudget == null || estimatedCost.compareTo(remainingBudget) <= 0;
    }

    private BigDecimal safeCost(BigDecimal cost) {
        return cost != null ? cost : BigDecimal.ZERO;
    }

    private boolean hasUsableStart(TravelContext ctx) {
        return Math.abs(ctx.startLat()) > 0.0001 || Math.abs(ctx.startLon()) > 0.0001;
    }

    private record RoutingInfo(double distanceKm, int durationMinutes) {}
    
    private int estimateTravelMinutes(double km, Transportation transport) {
        if (km < 0.05) {
            return 0;
        }
        Transportation effectiveTransport = transport != null ? transport : Transportation.MOTORBIKE;
        double speedKmh = switch (effectiveTransport) {
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
