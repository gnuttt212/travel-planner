package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.Transportation;
import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.domain.TripDuration;
import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.domain.DestinationCategory;
import com.travelplanner.recommendation.domain.ScoredDestination;
import com.travelplanner.planning.domain.TripActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SlotAllocatorTest {

    private SlotAllocator allocator;

    @BeforeEach
    void setUp() {
        // Pass null for ORS client in test, which will cause it to throw exception (or we mock it)
        // Since we want to test fallback, we can pass a mock that throws exception.
        com.travelplanner.recommendation.integration.OpenRouteServiceClient mockClient = org.mockito.Mockito.mock(com.travelplanner.recommendation.integration.OpenRouteServiceClient.class);
        org.mockito.Mockito.when(mockClient.getRoute(org.mockito.ArgumentMatchers.anyDouble(), org.mockito.ArgumentMatchers.anyDouble(), org.mockito.ArgumentMatchers.anyDouble(), org.mockito.ArgumentMatchers.anyDouble(), org.mockito.ArgumentMatchers.any()))
            .thenThrow(new RuntimeException("ORS down"));
            
        allocator = new SlotAllocator(mockClient);
    }

    @Test
    void testAllocate_HalfDay_Max3Activities() {
        TravelContext ctx = TravelContext.builder()
                .duration(TripDuration.HALF_DAY)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.now())
                .build();

        List<ScoredDestination> candidates = createCandidates(5);
        List<TripActivity> plan = allocator.allocate(candidates, ctx);

        assertTrue(plan.size() <= 3, "Half day should have max 3 activities");
        assertEquals(0, plan.get(0).getOrderIndex());
        assertEquals(candidates.get(0).destination().getId(), plan.get(0).getDestinationId());
    }

    @Test
    void testAllocate_IncludesTravelTime() {
        TravelContext ctx = TravelContext.builder()
                .duration(TripDuration.FULL_DAY)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.now())
                .build();

        // Dest 1 at origin, Dest 2 is ~11km away
        Destination d1 = Destination.builder().id(UUID.randomUUID()).category(DestinationCategory.CAFE).latitude(10.0).longitude(106.0).build();
        Destination d2 = Destination.builder().id(UUID.randomUUID()).category(DestinationCategory.MUSEUM).latitude(10.1).longitude(106.0).build(); // 1 degree lat is ~111km, so 0.1 is ~11km

        List<ScoredDestination> candidates = List.of(
                new ScoredDestination(d1, null),
                new ScoredDestination(d2, null)
        );

        List<TripActivity> plan = allocator.allocate(candidates, ctx);
        assertEquals(2, plan.size());

        TripActivity act1 = plan.get(0);
        TripActivity act2 = plan.get(1);

        // First activity has default travel buffer from hotel
        assertEquals(15, act1.getTravelTimeMinutes());
        
        // Second activity should have travel time > 0
        assertTrue(act2.getTravelTimeMinutes() > 0, "Should include travel time for second location");
        
        // Check if start time respects the travel buffer
        assertEquals(act1.getPlannedEndTime().plusMinutes(act2.getTravelTimeMinutes()), act2.getPlannedStartTime());
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
}
