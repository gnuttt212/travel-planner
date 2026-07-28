package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.recommendation.domain.CompositeScore;
import com.travelplanner.recommendation.domain.Destination;
import org.springframework.stereotype.Component;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

@Component
public class ScoringEngine {
    
    private static final double W_RATING = 0.20;
    private static final double W_DISTANCE = 0.25;
    private static final double W_HOURS = 0.20;
    private static final double W_PREFERENCE = 0.20;
    private static final double W_BUDGET = 0.15;
    
    // Global averages for Bayesian rating (will be calculated dynamically later)
    private static final double GLOBAL_AVG_RATING = 3.5;
    private static final int GLOBAL_AVG_REVIEW_COUNT = 50;
    
    public CompositeScore score(Destination dest, TravelContext ctx, boolean isRaining) {
        double R = scoreRating(dest);
        double D = scoreDistance(dest, ctx);
        double H = scoreHours(dest, ctx);
        double U = scorePreference(dest, ctx);
        double B = scoreBudget(dest, ctx);
        
        double total = W_RATING * R + W_DISTANCE * D + W_HOURS * H + W_PREFERENCE * U + W_BUDGET * B;
        
        // Weather adjustment
        if (isRaining) {
            if (!dest.isIndoor()) {
                total = total * 0.4; // 60% penalty for outdoor when raining
            } else {
                total = Math.min(1.0, total * 1.2); // 20% bonus for indoor when raining
            }
        }
        
        return new CompositeScore(total, R, D, H, U, B);
    }
    
    private double scoreRating(Destination dest) {
        // Bayesian Average
        double C = GLOBAL_AVG_REVIEW_COUNT;
        double m = GLOBAL_AVG_RATING;
        double n = dest.getReviewCount();
        double sum = dest.getAvgRating() * n;
        double bayesian = (C * m + sum) / (C + n);
        return (bayesian - 1.0) / 4.0; // normalize 1-5 → 0-1
    }
    
    private double scoreDistance(Destination dest, TravelContext ctx) {
        double distance = haversineKm(ctx.startLat(), ctx.startLon(), dest.getLatitude(), dest.getLongitude());
        double maxRadius = ctx.transportation().getMaxRadiusKm();
        return Math.max(0, 1.0 - (distance / maxRadius));
    }
    
    private double scoreHours(Destination dest, TravelContext ctx) {
        if (dest.getOpeningHours() == null || dest.getOpeningHours().isEmpty()) {
            return 0.7; // assume mostly open if no data
        }
        try {
            String dayKey = ctx.date().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toLowerCase();
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, String> hours = mapper.readValue(dest.getOpeningHours(), Map.class);
            String dayHours = hours.get(dayKey);
            if (dayHours == null || dayHours.equalsIgnoreCase("closed")) return 0.0;
            if (dayHours.equalsIgnoreCase("24h")) return 1.0;
            // Parse "08:00-22:00" format
            String[] parts = dayHours.split("-");
            LocalTime open = LocalTime.parse(parts[0].trim());
            LocalTime close = LocalTime.parse(parts[1].trim());
            LocalTime planned = LocalTime.of(10, 0); // default morning start
            if (!planned.isBefore(open) && planned.isBefore(close)) {
                // Check if enough time remains
                long minutesLeft = java.time.Duration.between(planned, close).toMinutes();
                return minutesLeft >= 60 ? 1.0 : 0.5;
            }
            return 0.0;
        } catch (Exception e) {
            return 0.7;
        }
    }
    
    private double scorePreference(Destination dest, TravelContext ctx) {
        if (ctx.styles() == null || ctx.styles().isEmpty()) return 0.5;
        List<String> destTags = dest.getTagList();
        if (destTags.isEmpty()) return 0.3;
        long matches = ctx.styles().stream()
            .filter(s -> destTags.stream().anyMatch(t -> t.equalsIgnoreCase(s)))
            .count();
        return (double) matches / ctx.styles().size();
    }
    
    private double scoreBudget(Destination dest, TravelContext ctx) {
        if (ctx.budgetPerPerson() == null) return 0.8; // unlimited budget
        if (dest.getAvgCostPerPerson() == null) return 0.5;
        double ratio = dest.getAvgCostPerPerson().doubleValue() / ctx.budgetPerPerson().doubleValue();
        if (ratio <= 0.7) return 1.0;
        if (ratio <= 1.0) return 0.7;
        if (ratio <= 1.3) return 0.3;
        return 0.0;
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
