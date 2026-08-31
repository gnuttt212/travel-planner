package com.travelplanner.recommendation.service;

import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.domain.Transportation;
import com.travelplanner.recommendation.domain.*;
import com.travelplanner.recommendation.engine.*;
import com.travelplanner.recommendation.integration.NominatimGeocodingClient;
import com.travelplanner.recommendation.port.DestinationSearchPort;
import com.travelplanner.recommendation.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

import com.travelplanner.recommendation.integration.OpenWeatherMapServiceClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    
    private final DestinationRepository destinationRepository;
    private final ScoringEngine scoringEngine;
    private final PlanBuilder planBuilder;
    private final OpenWeatherMapServiceClient weatherClient;
    private final DestinationSearchPort destinationSearchPort;
    private final NominatimGeocodingClient geocodingClient;

    /** Whether dynamic enrichment via external API (Overpass) is enabled */
    @Value("${destination.dynamic-search.enabled:true}")
    private boolean dynamicSearchEnabled;

    /** Minimum number of candidates before triggering external search */
    @Value("${destination.dynamic-search.min-candidates-threshold:5}")
    private int minCandidatesThreshold;

    /** Search radius in kilometers for external POI search */
    @Value("${destination.dynamic-search.search-radius-km:15}")
    private double searchRadiusKm;

    /**
     * Core recommendation logic for Trip creation (Flow A).
     * <p>
     * This is the single entry point used by {@code RecommendationPortImpl} to generate
     * trip plan variants. The flow is:
     * <ol>
     *   <li>Check weather conditions (penalize outdoor if raining)</li>
     *   <li>Query candidates from DB</li>
     *   <li><strong>Dynamic enrichment</strong>: if candidates are below threshold and
     *       dynamic search is enabled, geocode the city and fetch POIs from external
     *       source (Overpass/OSM), deduplicate, upsert into DB, and merge</li>
     *   <li>Score each candidate via ScoringEngine</li>
     *   <li>Build plan variants via PlanBuilder</li>
     * </ol>
     *
     * @param ctx  travel context (date, budget, transportation, etc.)
     * @param city target city name
     * @return list of trip plan variants, sorted by score
     */
    public List<TripPlanVariant> recommend(TravelContext ctx, String city) {
        List<Destination> candidates = loadCandidates(city, ctx);
        TravelContext effectiveCtx = alignStartWithCityIfClearlyWrong(ctx, candidates, city);

        // Step 1: Get candidates from DB
        candidates = filterReachableCandidates(candidates, effectiveCtx);
        log.info("Found {} reachable candidates for city={}, budget={}", candidates.size(), city, effectiveCtx.budgetPerPerson());

        // Step 3: Dynamic enrichment — if we have too few candidates, try to fetch
        // more from an external source (Overpass/OSM) and persist them into DB.
        // This ensures future requests for the same city will find data in DB directly.
        if (dynamicSearchEnabled && candidates.size() < minCandidatesThreshold) {
            candidates = enrichFromExternalSource(candidates, city);
            effectiveCtx = alignStartWithCityIfClearlyWrong(effectiveCtx, candidates, city);
            candidates = filterReachableCandidates(candidates, effectiveCtx);
        }

        // Step 3: Query Weather
        boolean isRaining = weatherClient.isRaining(effectiveCtx.startLat(), effectiveCtx.startLon(), effectiveCtx.date());
        if (isRaining) {
            log.info("Rain detected for {}. Outdoor destinations will be penalized.", effectiveCtx.date());
        }

        // Step 4: Score each candidate
        TravelContext scoringCtx = effectiveCtx;
        List<ScoredDestination> scored = candidates.stream()
            .map(dest -> new ScoredDestination(dest, scoringEngine.score(dest, scoringCtx, isRaining)))
            .sorted()
            .collect(Collectors.toList());
        
        log.info("Scored {} destinations. Top: {} (score={})", 
            scored.size(),
            scored.isEmpty() ? "N/A" : scored.get(0).destination().getName(),
            scored.isEmpty() ? 0 : scored.get(0).score().total());
        
        // Step 5: Build plan variants
        return planBuilder.buildVariants(scored, scoringCtx);
    }

    private List<Destination> loadCandidates(String city, TravelContext ctx) {
        if (ctx.budgetPerPerson() == null) {
            return destinationRepository.findByCity(city);
        }

        List<Destination> candidates = destinationRepository.findCandidates(city, ctx.budgetPerPerson());
        if (!candidates.isEmpty()) {
            return candidates;
        }

        return destinationRepository.findByCity(city);
    }

    private TravelContext alignStartWithCityIfClearlyWrong(TravelContext ctx, List<Destination> candidates, String city) {
        if (candidates.isEmpty() || !hasUsableStart(ctx)) {
            return ctx;
        }

        double centerLat = candidates.stream().mapToDouble(Destination::getLatitude).average().orElse(ctx.startLat());
        double centerLon = candidates.stream().mapToDouble(Destination::getLongitude).average().orElse(ctx.startLon());
        double distanceToCityCenter = haversineKm(ctx.startLat(), ctx.startLon(), centerLat, centerLon);
        double mismatchThresholdKm = Math.max(40.0, maxRouteRadiusKm(ctx) * 2.0);

        if (distanceToCityCenter <= mismatchThresholdKm) {
            return ctx;
        }

        log.warn(
                "Start coordinates are {} km away from city='{}'. Using city centroid lat={}, lon={} for recommendation scoring.",
                Math.round(distanceToCityCenter),
                city,
                centerLat,
                centerLon);
        return TravelContext.builder()
                .purpose(ctx.purpose())
                .date(ctx.date())
                .duration(ctx.duration())
                .groupType(ctx.groupType())
                .groupSize(ctx.groupSize())
                .budgetPerPerson(ctx.budgetPerPerson())
                .styles(ctx.styles())
                .startLat(centerLat)
                .startLon(centerLon)
                .transportation(ctx.transportation())
                .build();
    }

    private List<Destination> filterReachableCandidates(List<Destination> candidates, TravelContext ctx) {
        if (candidates.isEmpty()) {
            return candidates;
        }

        java.math.BigDecimal maxAllowedCost = null;
        if (ctx.budgetPerPerson() != null && ctx.budgetPerPerson().compareTo(java.math.BigDecimal.ZERO) > 0) {
            // Hard filter: drop any single destination that costs more than 1.5x the TOTAL budget
            maxAllowedCost = ctx.budgetPerPerson().multiply(java.math.BigDecimal.valueOf(1.5));
        }
        
        final java.math.BigDecimal finalMaxCost = maxAllowedCost;

        return candidates.stream()
                .filter(dest -> {
                    if (finalMaxCost != null && dest.getAvgCostPerPerson() != null) {
                        if (dest.getAvgCostPerPerson().compareTo(finalMaxCost) > 0) return false;
                    }
                    if (hasUsableStart(ctx)) {
                        double maxRadiusKm = maxRouteRadiusKm(ctx);
                        if (haversineKm(ctx.startLat(), ctx.startLon(), dest.getLatitude(), dest.getLongitude()) > maxRadiusKm) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private double maxRouteRadiusKm(TravelContext ctx) {
        double transportMax = effectiveTransportation(ctx).getMaxRadiusKm();
        if (ctx.duration() == null) {
            return transportMax;
        }
        return switch (ctx.duration()) {
            case HALF_DAY -> Math.min(transportMax, 20.0);
            case FULL_DAY -> Math.min(transportMax, 60.0);
            case MULTI_DAY -> transportMax;
        };
    }

    private Transportation effectiveTransportation(TravelContext ctx) {
        return ctx.transportation() != null
                ? ctx.transportation()
                : Transportation.MOTORBIKE;
    }

    private boolean hasUsableStart(TravelContext ctx) {
        return Math.abs(ctx.startLat()) > 0.0001 || Math.abs(ctx.startLon()) > 0.0001;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    /**
     * Enrich the candidate list by fetching POIs from an external source when the DB
     * has insufficient data for the requested city.
     * <p>
     * Flow:
     * <ol>
     *   <li>Geocode the city name to get lat/lon (via NominatimGeocodingClient)</li>
     *   <li>Call DestinationSearchPort.searchNearby() to fetch POIs from external API</li>
     *   <li>Deduplicate: remove any external results whose name already exists in the
     *       DB for this city (case-insensitive match) to prevent duplicate entries</li>
     *   <li>Upsert: save new destinations to DB so subsequent requests use DB data</li>
     *   <li>Merge: combine existing DB candidates with newly saved destinations</li>
     * </ol>
     *
     * @param existingCandidates current candidates from DB (may be empty)
     * @param city               target city name
     * @return merged list of existing + new candidates
     */
    private List<Destination> enrichFromExternalSource(List<Destination> existingCandidates, String city) {
        log.info("Candidates ({}) below threshold ({}), attempting dynamic enrichment for city={}",
                existingCandidates.size(), minCandidatesThreshold, city);

        // Step 3a: Geocode city to get coordinates for the search
        NominatimGeocodingClient.GeocodingResult geocoded = geocodingClient.geocode(city + ", Vietnam");
        if (geocoded == null) {
            log.warn("Could not geocode city='{}', skipping dynamic enrichment", city);
            return existingCandidates;
        }
        log.info("Geocoded city='{}' to lat={}, lon={}", city, geocoded.lat(), geocoded.lon());

        // Step 3b: Search nearby POIs from external source (Overpass API)
        List<Destination> externalResults = destinationSearchPort.searchNearby(
                geocoded.lat(), geocoded.lon(), searchRadiusKm, city);

        if (externalResults.isEmpty()) {
            log.info("No external results found for city={}", city);
            return existingCandidates;
        }
        log.info("Fetched {} external results for city={}", externalResults.size(), city);

        // Step 3c: Deduplicate — collect names of ALL destinations already in DB for this city
        // (not just the budget-filtered candidates) to avoid inserting duplicates.
        // Uses the same city condition as findCandidates() to ensure consistency.
        Set<String> existingNames = destinationRepository.findByCity(city).stream()
                .map(d -> d.getName().trim().toLowerCase())
                .collect(Collectors.toSet());

        List<Destination> newDestinations = externalResults.stream()
                .filter(d -> d.getName() != null && !d.getName().isBlank())
                .filter(d -> !existingNames.contains(d.getName().trim().toLowerCase()))
                .collect(Collectors.toList());

        if (newDestinations.isEmpty()) {
            log.info("All external results already exist in DB for city={}", city);
            return existingCandidates;
        }

        // Step 3d: Upsert — save new destinations to DB for future requests
        List<Destination> saved = destinationRepository.saveAll(newDestinations);
        log.info("Upserted {} new destinations into DB for city={}", saved.size(), city);

        // Step 3e: Merge — combine existing candidates with newly saved destinations
        List<Destination> merged = new ArrayList<>(existingCandidates);
        merged.addAll(saved);
        return merged;
    }
}
