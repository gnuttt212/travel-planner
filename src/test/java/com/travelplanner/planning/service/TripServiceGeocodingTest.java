package com.travelplanner.planning.service;

import com.travelplanner.planning.domain.Trip;
import com.travelplanner.planning.dto.TripRequest;
import com.travelplanner.planning.port.RecommendationPort;
import com.travelplanner.planning.repository.TripRepository;
import com.travelplanner.recommendation.integration.NominatimGeocodingClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceGeocodingTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RecommendationPort recommendationPort;

    @Mock
    private TripActivityMapper activityMapper;

    @Mock
    private NominatimGeocodingClient geocodingClient;

    @InjectMocks
    private TripService tripService;

    @Test
    void testGetById_ExistingTrip_ShouldNotCallGeocode() {
        // Arrange
        UUID tripId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).build();
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // Act
        tripService.getById(tripId);

        // Assert
        verify(geocodingClient, never()).geocode(any());
        verify(geocodingClient, never()).fetchGeocodeCached(any(), any());
    }
}
