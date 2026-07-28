package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.*;
import com.travelplanner.recommendation.domain.*;
import com.travelplanner.recommendation.integration.GeminiNarrativeService;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanBuilder {
    
    private final SlotAllocator slotAllocator;
    private final GeminiNarrativeService geminiService;
    
    public List<TripPlanVariant> buildVariants(List<ScoredDestination> allScored, TravelContext ctx) {
        List<TripPlanVariant> variants = new ArrayList<>();
        
        // Variant A: Balanced
        List<TripActivity> balanced = slotAllocator.allocate(allScored, ctx);
        
        // Variant B: Food-focused
        List<ScoredDestination> foodBoosted = allScored.stream()
            .sorted((a, b) -> {
                boolean aFood = isFood(a.destination());
                boolean bFood = isFood(b.destination());
                if (aFood && !bFood) return -1;
                if (!aFood && bFood) return 1;
                return a.compareTo(b);
            })
            .collect(Collectors.toList());
        List<TripActivity> foodFocused = slotAllocator.allocate(foodBoosted, ctx);
        
        // Variant C: Budget-optimized
        List<ScoredDestination> budgetSorted = allScored.stream()
            .sorted(Comparator.comparing(sd -> sd.destination().getAvgCostPerPerson() != null ? sd.destination().getAvgCostPerPerson() : BigDecimal.ZERO))
            .collect(Collectors.toList());
        List<TripActivity> budgetOpt = slotAllocator.allocate(budgetSorted, ctx);

        // Generate narratives asynchronously
        CompletableFuture<String> balancedFuture = CompletableFuture.supplyAsync(() -> 
            geminiService.generateNarrative("Cân bằng", getDestinations(allScored, balanced)));
            
        CompletableFuture<String> foodFuture = CompletableFuture.supplyAsync(() -> 
            geminiService.generateNarrative("Ẩm thực", getDestinations(allScored, foodFocused)));
            
        CompletableFuture<String> budgetFuture = CompletableFuture.supplyAsync(() -> 
            geminiService.generateNarrative("Tiết kiệm", getDestinations(allScored, budgetOpt)));

        CompletableFuture.allOf(balancedFuture, foodFuture, budgetFuture).join();

        variants.add(new TripPlanVariant("Cân bằng", "Kết hợp đa dạng trải nghiệm", balancedFuture.join(), balanced, calcTotalCost(balanced), calcTotalDistance(balanced)));
        variants.add(new TripPlanVariant("Ẩm thực", "Ưu tiên trải nghiệm ăn uống", foodFuture.join(), foodFocused, calcTotalCost(foodFocused), calcTotalDistance(foodFocused)));
        variants.add(new TripPlanVariant("Tiết kiệm", "Chi phí thấp nhất", budgetFuture.join(), budgetOpt, calcTotalCost(budgetOpt), calcTotalDistance(budgetOpt)));
        
        return variants;
    }
    
    private List<Destination> getDestinations(List<ScoredDestination> allScored, List<TripActivity> activities) {
        return activities.stream()
            .map(act -> allScored.stream()
                .filter(sd -> sd.destination().getId().equals(act.getDestinationId()))
                .findFirst()
                .map(ScoredDestination::destination)
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    private boolean isFood(Destination d) {
        return d.getCategory() == DestinationCategory.RESTAURANT || 
               d.getCategory() == DestinationCategory.STREET_FOOD ||
               d.getCategory() == DestinationCategory.CAFE ||
               d.getCategory() == DestinationCategory.MARKET;
    }
    
    private BigDecimal calcTotalCost(List<TripActivity> activities) {
        return activities.stream()
            .map(a -> a.getEstimatedCost() != null ? a.getEstimatedCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private double calcTotalDistance(List<TripActivity> activities) {
        return activities.stream().mapToDouble(TripActivity::getTravelDistanceKm).sum();
    }
}
