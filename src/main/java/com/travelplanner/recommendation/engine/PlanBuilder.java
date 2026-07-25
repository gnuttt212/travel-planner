package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.*;
import com.travelplanner.recommendation.domain.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanBuilder {
    
    private final SlotAllocator slotAllocator;
    
    public List<TripPlanVariant> buildVariants(List<ScoredDestination> allScored, TravelContext ctx) {
        List<TripPlanVariant> variants = new ArrayList<>();
        
        // Variant A: Balanced (use top scores as-is)
        List<TripActivity> balanced = slotAllocator.allocate(allScored, ctx);
        variants.add(new TripPlanVariant("Cân bằng", "Kết hợp đa dạng trải nghiệm", balanced, calcTotalCost(balanced), calcTotalDistance(balanced)));
        
        // Variant B: Food-focused (boost restaurant/cafe scores)
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
        variants.add(new TripPlanVariant("Ẩm thực", "Ưu tiên trải nghiệm ăn uống", foodFocused, calcTotalCost(foodFocused), calcTotalDistance(foodFocused)));
        
        // Variant C: Budget-optimized (sort by cost ascending, then score)
        List<ScoredDestination> budgetSorted = allScored.stream()
            .sorted(Comparator.comparing(sd -> sd.destination().getAvgCostPerPerson() != null ? sd.destination().getAvgCostPerPerson() : BigDecimal.ZERO))
            .collect(Collectors.toList());
        List<TripActivity> budgetOpt = slotAllocator.allocate(budgetSorted, ctx);
        variants.add(new TripPlanVariant("Tiết kiệm", "Chi phí thấp nhất", budgetOpt, calcTotalCost(budgetOpt), calcTotalDistance(budgetOpt)));
        
        return variants;
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
