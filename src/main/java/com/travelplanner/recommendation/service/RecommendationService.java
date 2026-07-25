package com.travelplanner.recommendation.service;

import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.recommendation.domain.*;
import com.travelplanner.recommendation.engine.*;
import com.travelplanner.recommendation.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {
    
    private final DestinationRepository destinationRepository;
    private final ScoringEngine scoringEngine;
    private final PlanBuilder planBuilder;
    
    public List<TripPlanVariant> recommend(TravelContext ctx, String city) {
        // Step 1: Get candidates from DB
        List<Destination> candidates = destinationRepository.findCandidates(city, ctx.budgetPerPerson());
        log.info("Found {} candidates for city={}, budget={}", candidates.size(), city, ctx.budgetPerPerson());
        
        if (candidates.isEmpty()) {
            // Fallback: get all destinations in city without budget filter
            candidates = destinationRepository.findByCity(city);
        }
        
        // Step 2: Score each candidate
        List<ScoredDestination> scored = candidates.stream()
            .map(dest -> new ScoredDestination(dest, scoringEngine.score(dest, ctx)))
            .sorted()
            .collect(Collectors.toList());
        
        log.info("Scored {} destinations. Top: {} (score={})", 
            scored.size(),
            scored.isEmpty() ? "N/A" : scored.get(0).destination().getName(),
            scored.isEmpty() ? 0 : scored.get(0).score().total());
        
        // Step 3: Build plan variants
        return planBuilder.buildVariants(scored, ctx);
    }
}
