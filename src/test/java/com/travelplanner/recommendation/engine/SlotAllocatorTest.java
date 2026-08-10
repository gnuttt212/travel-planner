package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.Transportation;
import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.domain.TripDuration;
import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.domain.DestinationCategory;
import com.travelplanner.recommendation.domain.ScoredDestination;
import com.travelplanner.planning.domain.TripActivity;
import com.travelplanner.recommendation.integration.OpenRouteServiceClient;
import com.travelplanner.recommendation.integration.OsrmRoutingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotAllocatorTest {

    @Mock
    private OpenRouteServiceClient orsClient;
    
    @Mock
    private OsrmRoutingClient osrmClient;

    private SlotAllocator allocator;

    @BeforeEach
    void setUp() {
        allocator = new SlotAllocator(orsClient, osrmClient);
    }

    @Test
    void testFallbackChain_OrsSuccess_NoOsrmCall() {
        TravelContext ctx = TravelContext.builder()
                .duration(TripDuration.FULL_DAY)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.now())
                .build();

        List<ScoredDestination> candidates = createCandidates(2);

        when(orsClient.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(new OpenRouteServiceClient.RoutingInfo(10.5, 20));

        List<TripActivity> plan = allocator.allocate(candidates, ctx);

        assertEquals(2, plan.size());
        assertEquals(20, plan.get(1).getTravelTimeMinutes());
        assertEquals(10.5, plan.get(1).getTravelDistanceKm());

        // Verify ORS was called and OSRM was NEVER called
        verify(orsClient, times(1)).getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
        verify(osrmClient, never()).route(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void testFallbackChain_OrsFails_OsrmSuccess() {
        TravelContext ctx = TravelContext.builder()
                .duration(TripDuration.FULL_DAY)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.now())
                .build();

        List<ScoredDestination> candidates = createCandidates(2);

        when(orsClient.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenThrow(new RuntimeException("ORS timeout"));
                
        when(osrmClient.route(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new OsrmRoutingClient.RoutingResult(15.2, 30));

        List<TripActivity> plan = allocator.allocate(candidates, ctx);

        assertEquals(2, plan.size());
        assertEquals(30, plan.get(1).getTravelTimeMinutes());
        assertEquals(15.2, plan.get(1).getTravelDistanceKm());

        // Verify both were called
        verify(orsClient, times(1)).getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
        verify(osrmClient, times(1)).route(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }
    
    @Test
    void testFallbackChain_OrsFails_OsrmFails_HaversineSuccess() {
        TravelContext ctx = TravelContext.builder()
                .duration(TripDuration.FULL_DAY)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.now())
                .build();

        // Dest 1 at origin, Dest 2 is ~11km away (0.1 degree lat difference)
        Destination d1 = Destination.builder().id(UUID.randomUUID()).category(DestinationCategory.CAFE).latitude(10.0).longitude(106.0).build();
        Destination d2 = Destination.builder().id(UUID.randomUUID()).category(DestinationCategory.MUSEUM).latitude(10.1).longitude(106.0).build(); 
        List<ScoredDestination> candidates = List.of(new ScoredDestination(d1, null), new ScoredDestination(d2, null));

        when(orsClient.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenThrow(new RuntimeException("ORS fails"));
                
        when(osrmClient.route(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("OSRM fails"));

        // Should not throw exception
        List<TripActivity> plan = allocator.allocate(candidates, ctx);

        assertEquals(2, plan.size());
        TripActivity act2 = plan.get(1);
        
        // Haversine fallback should be calculated
        // distance for 0.1 degree is roughly 11.1km
        assertTrue(act2.getTravelDistanceKm() > 10.0 && act2.getTravelDistanceKm() < 12.0, "Distance should be around 11km, got: " + act2.getTravelDistanceKm());
        
        // Time = 11km / 25km/h (motorbike) * 60 = ~26 mins
        assertTrue(act2.getTravelTimeMinutes() > 20 && act2.getTravelTimeMinutes() < 30, "Time should be ~26 mins, got: " + act2.getTravelTimeMinutes());

        verify(orsClient, times(1)).getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
        verify(osrmClient, times(1)).route(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void allocate_SkipsActivitiesThatWouldExceedTotalBudget() {
        TravelContext ctx = TravelContext.builder()
                .duration(TripDuration.FULL_DAY)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.now())
                .budgetPerPerson(BigDecimal.valueOf(70000))
                .build();

        Destination expensive = budgetCandidate("Expensive attraction", DestinationCategory.ENTERTAINMENT, 80000);
        Destination affordable = budgetCandidate("Affordable cafe", DestinationCategory.CAFE, 50000);
        Destination wouldExceedTotal = budgetCandidate("Second cafe", DestinationCategory.CAFE, 40000);
        List<ScoredDestination> candidates = List.of(
                new ScoredDestination(expensive, null),
                new ScoredDestination(affordable, null),
                new ScoredDestination(wouldExceedTotal, null)
        );

        List<TripActivity> plan = allocator.allocate(candidates, ctx);

        assertEquals(1, plan.size());
        assertEquals("Affordable cafe", plan.get(0).getDestinationName());
        assertEquals(BigDecimal.valueOf(50000), plan.get(0).getEstimatedCost());
    }

    private List<ScoredDestination> createCandidates(int count) {
        List<ScoredDestination> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Destination dest = Destination.builder()
                    .id(UUID.randomUUID())
                    .name("Place " + i)
                    .category(DestinationCategory.PARK)
                    .latitude(10.0 + (i * 0.01))
                    .longitude(106.0)
                    .build();
            list.add(new ScoredDestination(dest, null));
        }
        return list;
    }

    private Destination budgetCandidate(String name, DestinationCategory category, long cost) {
        return Destination.builder()
                .id(UUID.randomUUID())
                .name(name)
                .category(category)
                .latitude(10.0)
                .longitude(106.0)
                .avgCostPerPerson(BigDecimal.valueOf(cost))
                .build();
    }
}
