package com.travelplanner.planning.service;

import com.travelplanner.planning.domain.*;
import com.travelplanner.planning.port.RecommendationPort;
import com.travelplanner.planning.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceReorderTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RecommendationPort recommendationPort;

    @Mock
    private TripActivityMapper activityMapper;
    
    @Mock
    private com.travelplanner.recommendation.integration.NominatimGeocodingClient geocodingClient;

    @InjectMocks
    private TripService tripService;

    private Trip trip;
    private UUID tripId;
    private UUID actA, actB, actC;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        actA = UUID.randomUUID();
        actB = UUID.randomUUID();
        actC = UUID.randomUUID();

        // 3 activities: A(08:00-08:30) -> B(08:35-09:05) -> C(09:10-09:40)
        // All in HCM area with small lat/lon differences
        TripActivity a = TripActivity.builder()
                .id(actA).orderIndex(0)
                .plannedStartTime(LocalTime.of(8, 0))
                .plannedEndTime(LocalTime.of(8, 30))
                .estimatedDurationMinutes(30)
                .estimatedCost(BigDecimal.valueOf(50000))
                .destinationName("Bến Thành Market")
                .destinationCategory("MARKET")
                .destinationLat(10.7721)
                .destinationLon(106.6980)
                .travelDistanceKm(0).travelTimeMinutes(0)
                .status(ActivityStatus.UPCOMING)
                .build();

        TripActivity b = TripActivity.builder()
                .id(actB).orderIndex(1)
                .plannedStartTime(LocalTime.of(8, 35))
                .plannedEndTime(LocalTime.of(9, 5))
                .estimatedDurationMinutes(30)
                .estimatedCost(BigDecimal.valueOf(30000))
                .destinationName("War Museum")
                .destinationCategory("MUSEUM")
                .destinationLat(10.7799)
                .destinationLon(106.6922)
                .travelDistanceKm(1.0).travelTimeMinutes(5)
                .status(ActivityStatus.UPCOMING)
                .build();

        TripActivity c = TripActivity.builder()
                .id(actC).orderIndex(2)
                .plannedStartTime(LocalTime.of(9, 10))
                .plannedEndTime(LocalTime.of(9, 40))
                .estimatedDurationMinutes(30)
                .estimatedCost(BigDecimal.valueOf(20000))
                .destinationName("Notre Dame Cathedral")
                .destinationCategory("CULTURE")
                .destinationLat(10.7798)
                .destinationLon(106.6990)
                .travelDistanceKm(0.7).travelTimeMinutes(5)
                .status(ActivityStatus.UPCOMING)
                .build();

        trip = Trip.builder()
                .id(tripId)
                .ownerId("owner@test.com")
                .title("Test Trip")
                .status(TripStatus.PLANNED)
                .transportation(Transportation.MOTORBIKE)
                .activities(new ArrayList<>(List.of(a, b, c)))
                .build();
    }

    @Test
    void reorderActivities_ValidReorder_UpdatesOrderAndTimes() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        // Reverse order: C, B, A
        List<UUID> newOrder = List.of(actC, actB, actA);
        Trip result = tripService.reorderActivities(tripId, newOrder);

        List<TripActivity> activities = result.getActivities();

        // Verify orderIndex
        TripActivity reorderedC = activities.stream().filter(a -> a.getId().equals(actC)).findFirst().orElseThrow();
        TripActivity reorderedB = activities.stream().filter(a -> a.getId().equals(actB)).findFirst().orElseThrow();
        TripActivity reorderedA = activities.stream().filter(a -> a.getId().equals(actA)).findFirst().orElseThrow();

        assertEquals(0, reorderedC.getOrderIndex(), "C should be at index 0");
        assertEquals(1, reorderedB.getOrderIndex(), "B should be at index 1");
        assertEquals(2, reorderedA.getOrderIndex(), "A should be at index 2");

        // First activity starts at trip start time (08:00)
        assertEquals(LocalTime.of(8, 0), reorderedC.getPlannedStartTime(),
                "First activity should keep trip start time");
        assertEquals(0, reorderedC.getTravelDistanceKm(), 0.001,
                "First activity should have 0 travel distance");
        assertEquals(0, reorderedC.getTravelTimeMinutes(),
                "First activity should have 0 travel time");

        // Duration preserved: each is 30 min
        assertEquals(LocalTime.of(8, 30), reorderedC.getPlannedEndTime());
        assertEquals(30, reorderedC.getEstimatedDurationMinutes());

        // Second activity: starts after C's end + travel time from C to B
        assertTrue(reorderedB.getPlannedStartTime().isAfter(reorderedC.getPlannedEndTime()) 
                   || reorderedB.getPlannedStartTime().equals(reorderedC.getPlannedEndTime().plusMinutes(reorderedB.getTravelTimeMinutes())),
                "B should start after C ends + travel time");
        assertTrue(reorderedB.getTravelDistanceKm() > 0, "Travel distance B should be > 0");
        assertTrue(reorderedB.getTravelTimeMinutes() >= 5, "Travel time B should be >= 5 min (Haversine minimum)");

        // Third activity: starts after B's end + travel time from B to A
        assertEquals(reorderedB.getPlannedEndTime().plusMinutes(reorderedA.getTravelTimeMinutes()),
                reorderedA.getPlannedStartTime(),
                "A should start exactly at B.end + travelTime(B->A)");
        assertEquals(reorderedA.getPlannedStartTime().plusMinutes(30), reorderedA.getPlannedEndTime(),
                "A endTime = startTime + 30 min duration");

        // updatedAt should be set
        assertNotNull(result.getUpdatedAt());

        verify(tripRepository).save(trip);
    }

    @Test
    void reorderActivities_MissingActivityId_ThrowsException() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // Only 2 of 3 IDs
        List<UUID> incomplete = List.of(actA, actB);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tripService.reorderActivities(tripId, incomplete));

        assertTrue(ex.getMessage().contains("không khớp"),
                "Message should indicate mismatch");

        // Must NOT save
        verify(tripRepository, never()).save(any());
    }

    @Test
    void reorderActivities_ForeignActivityId_ThrowsException() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        UUID foreignId = UUID.randomUUID();
        List<UUID> withForeign = List.of(actA, actB, foreignId);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tripService.reorderActivities(tripId, withForeign));

        assertTrue(ex.getMessage().contains("không khớp"),
                "Message should indicate mismatch");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void reorderActivities_DuplicateActivityId_ThrowsException() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        List<UUID> withDupe = List.of(actA, actA, actB);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tripService.reorderActivities(tripId, withDupe));

        assertTrue(ex.getMessage().contains("trùng lặp"),
                "Message should indicate duplicate");

        verify(tripRepository, never()).save(any());
    }
}
