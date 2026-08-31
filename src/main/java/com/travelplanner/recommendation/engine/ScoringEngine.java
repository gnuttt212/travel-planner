package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.recommendation.domain.CompositeScore;
import com.travelplanner.recommendation.domain.Destination;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

import com.travelplanner.planning.domain.Transportation;

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
        double R = scoreReliability(dest);
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
    
    private double scoreReliability(Destination dest) {
        // Bayesian Average
        double C = GLOBAL_AVG_REVIEW_COUNT;
        double m = GLOBAL_AVG_RATING;
        double n = dest.getReviewCount();
        double sum = dest.getAvgRating() * n;
        double bayesian = (C * m + sum) / (C + n);
        
        double score = (bayesian - 1.0) / 4.0; // normalize 1-5 → 0-1
        
        // Apply penalty for places with very few reviews to ensure reliability
        if (n < 5) {
            score = score * 0.5; // 50% penalty
        } else if (n < 20) {
            score = score * 0.8; // 20% penalty
        }
        
        return Math.max(0.0, score);
    }
    
    private double scoreDistance(Destination dest, TravelContext ctx) {
        double distance = haversineKm(ctx.startLat(), ctx.startLon(), dest.getLatitude(), dest.getLongitude());
        double maxRadius = effectiveTransportation(ctx).getMaxRadiusKm();
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
        double baseScore = 0.5;
        
        // Enhance base score if there are explicit styles provided by user
        if (ctx.styles() != null && !ctx.styles().isEmpty()) {
            List<String> destTags = dest.getTagList();
            if (!destTags.isEmpty()) {
                long matches = ctx.styles().stream()
                    .filter(s -> destTags.stream().anyMatch(t -> t.equalsIgnoreCase(s)))
                    .count();
                baseScore = (double) matches / ctx.styles().size();
            }
        }
        
        // 1. Group Type Mapping
        String groupType = ctx.groupType();
        if (groupType != null) {
            switch (groupType.toUpperCase()) {
                case "FAMILY":
                    if (dest.isIndoor()) baseScore += 0.2;
                    if (hasAnyTag(dest, "kid-friendly", "family", "accessible", "spacious")) baseScore += 0.3;
                    if (dest.getCategory() != null) {
                        String cat = dest.getCategory().name();
                        if (cat.equals("BAR") || cat.equals("STREET_FOOD")) baseScore -= 0.4;
                    }
                    break;
                case "COUPLE":
                    if (hasAnyTag(dest, "romantic", "private", "scenic", "quiet", "view")) baseScore += 0.3;
                    if (dest.getCategory() != null && dest.getCategory().name().equals("VIEWPOINT")) baseScore += 0.2;
                    break;
                case "SOLO":
                    if (hasAnyTag(dest, "street_food", "cafe", "cheap", "local")) baseScore += 0.2;
                    break;
                case "FRIENDS":
                    if (hasAnyTag(dest, "bar", "entertainment", "group", "fun", "lively")) baseScore += 0.3;
                    break;
            }
        }
        
        // 2. Trip Purpose Mapping
        if (ctx.purpose() != null) {
            switch (ctx.purpose().name()) {
                case "PHOTOGRAPHY":
                    if (dest.getCategory() != null) {
                        String cat = dest.getCategory().name();
                        if (cat.equals("VIEWPOINT") || cat.equals("GARDEN") || cat.equals("TEMPLE")) baseScore += 0.3;
                    }
                    if (hasAnyTag(dest, "scenic", "beautiful", "architecture", "photo")) baseScore += 0.3;
                    break;
                case "FOOD_EXPLORE":
                    if (dest.getCategory() != null) {
                        String cat = dest.getCategory().name();
                        if (cat.equals("RESTAURANT") || cat.equals("STREET_FOOD") || cat.equals("MARKET")) baseScore += 0.3;
                    }
                    if (hasAnyTag(dest, "local_food", "famous", "specialty")) baseScore += 0.2;
                    break;
                case "RELAXATION":
                    if (dest.getCategory() != null) {
                        String cat = dest.getCategory().name();
                        if (cat.equals("CAFE") || cat.equals("BEACH") || cat.equals("PARK")) baseScore += 0.3;
                    }
                    if (hasAnyTag(dest, "spa", "chill", "quiet", "relax")) baseScore += 0.2;
                    break;
                case "NATURE":
                    if (dest.getCategory() != null) {
                        String cat = dest.getCategory().name();
                        if (cat.equals("PARK") || cat.equals("GARDEN") || cat.equals("BEACH") || cat.equals("VIEWPOINT")) baseScore += 0.3;
                    }
                    if (hasAnyTag(dest, "nature", "outdoor", "forest", "mountain")) baseScore += 0.2;
                    if (!dest.isIndoor()) baseScore += 0.1;
                    else baseScore -= 0.1;
                    break;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, baseScore));
    }
    
    private boolean hasAnyTag(Destination dest, String... targetTags) {
        List<String> tags = dest.getTagList();
        if (tags == null || tags.isEmpty()) return false;
        for (String target : targetTags) {
            for (String tag : tags) {
                if (tag.toLowerCase().contains(target.toLowerCase())) return true;
            }
        }
        return false;
    }
    
    private double scoreBudget(Destination dest, TravelContext ctx) {
        if (ctx.budgetPerPerson() == null) return 0.8; // unlimited budget
        if (ctx.budgetPerPerson().compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        if (dest.getAvgCostPerPerson() == null) return 0.5;
        BigDecimal targetCostPerStop = ctx.budgetPerPerson().divide(
                BigDecimal.valueOf(targetActivityCount(ctx)),
                2,
                RoundingMode.HALF_UP);
        double ratio = dest.getAvgCostPerPerson().doubleValue() / targetCostPerStop.doubleValue();
        if (ratio <= 0.7) return 1.0;
        if (ratio <= 1.0) return 0.7;
        if (ratio <= 1.3) return 0.3;
        return 0.0;
    }

    private int targetActivityCount(TravelContext ctx) {
        if (ctx.duration() == null) {
            return 1;
        }
        return switch (ctx.duration()) {
            case HALF_DAY -> 3;
            case FULL_DAY -> 5;
            case MULTI_DAY -> 7;
        };
    }

    private Transportation effectiveTransportation(TravelContext ctx) {
        return ctx.transportation() != null ? ctx.transportation() : Transportation.MOTORBIKE;
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
