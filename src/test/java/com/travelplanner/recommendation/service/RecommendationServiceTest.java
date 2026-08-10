package com.travelplanner.recommendation.service;

import com.travelplanner.planning.domain.Transportation;
import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.domain.TripDuration;
import com.travelplanner.recommendation.domain.*;
import com.travelplanner.recommendation.engine.PlanBuilder;
import com.travelplanner.recommendation.engine.ScoringEngine;
import com.travelplanner.recommendation.integration.NominatimGeocodingClient;
import com.travelplanner.recommendation.integration.OpenWeatherMapServiceClient;
import com.travelplanner.recommendation.port.DestinationSearchPort;
import com.travelplanner.recommendation.repository.DestinationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock private DestinationRepository destinationRepository;
    @Mock private ScoringEngine scoringEngine;
    @Mock private PlanBuilder planBuilder;
    @Mock private OpenWeatherMapServiceClient weatherClient;
    @Mock private DestinationSearchPort destinationSearchPort;
    @Mock private NominatimGeocodingClient geocodingClient;

    @InjectMocks
    private RecommendationService service;

    private TravelContext ctx;

    @BeforeEach
    void setUp() {
        // Enable dynamic search and set threshold
        ReflectionTestUtils.setField(service, "dynamicSearchEnabled", true);
        ReflectionTestUtils.setField(service, "minCandidatesThreshold", 5);
        ReflectionTestUtils.setField(service, "searchRadiusKm", 15.0);

        ctx = TravelContext.builder()
                .startLat(10.762)
                .startLon(106.660)
                .transportation(Transportation.CAR)
                .date(LocalDate.now())
                .styles(List.of("CULTURAL"))
                .budgetPerPerson(new BigDecimal("500000"))
                .duration(TripDuration.HALF_DAY)
                .build();

        // Default: weather is fine
        when(weatherClient.isRaining(anyDouble(), anyDouble(), any())).thenReturn(false);

        // Default: scoring returns a dummy score
        when(scoringEngine.score(any(), any(), anyBoolean()))
                .thenReturn(new CompositeScore(0.5, 0.5, 0.5, 0.5, 0.5, 0.5));

        // Default: planBuilder returns empty list
        when(planBuilder.buildVariants(anyList(), any())).thenReturn(List.of());
    }

    /**
     * When DB has enough candidates (>= threshold), dynamic enrichment should NOT be triggered.
     */
    @Test
    void recommend_EnoughCandidates_DoesNotCallExternalSearch() {
        // 6 candidates >= threshold(5) → no enrichment needed
        List<Destination> dbCandidates = createDestinations("Đà Nẵng", 6);
        when(destinationRepository.findCandidates(eq("Đà Nẵng"), any())).thenReturn(dbCandidates);

        service.recommend(ctx, "Đà Nẵng");

        // Verify external search was NEVER called
        verify(destinationSearchPort, never()).searchNearby(anyDouble(), anyDouble(), anyDouble(), anyString());
        verify(geocodingClient, never()).geocode(anyString());
        // Verify no upsert happened
        verify(destinationRepository, never()).saveAll(anyList());
    }

    /**
     * When DB has fewer candidates than threshold, enrichment should be triggered:
     * geocode → searchNearby → dedup → saveAll → merge.
     */
    @Test
    void recommend_FewCandidates_TriggersEnrichmentAndUpsert() {
        // Only 2 candidates in DB — below threshold(5)
        List<Destination> dbCandidates = createDestinations("Huế", 2);
        when(destinationRepository.findCandidates(eq("Huế"), any())).thenReturn(dbCandidates);

        // Geocode returns valid result
        when(geocodingClient.geocode("Huế, Vietnam"))
                .thenReturn(new NominatimGeocodingClient.GeocodingResult(16.463, 107.590, "Huế, Vietnam"));

        // External search returns 4 new destinations
        List<Destination> externalResults = createDestinations("Huế", 4);
        // Give them different names to avoid dedup filtering
        for (int i = 0; i < externalResults.size(); i++) {
            externalResults.get(i).setName("External Place " + i);
        }
        when(destinationSearchPort.searchNearby(eq(16.463), eq(107.590), eq(15.0), eq("Huế")))
                .thenReturn(externalResults);

        // findByCity for dedup check — return same as dbCandidates
        when(destinationRepository.findByCity("Huế")).thenReturn(new ArrayList<>(dbCandidates));

        // saveAll returns the same list
        when(destinationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        service.recommend(ctx, "Huế");

        // Verify enrichment chain was called
        verify(geocodingClient).geocode("Huế, Vietnam");
        verify(destinationSearchPort).searchNearby(16.463, 107.590, 15.0, "Huế");
        verify(destinationRepository).saveAll(anyList());

        // Verify scoring received merged list (2 DB + 4 external = 6 total)
        verify(scoringEngine, times(6)).score(any(), any(), anyBoolean());
    }

    /**
     * When enrichment finds external results that already exist in DB (same name),
     * those duplicates should be filtered out and NOT saved again.
     */
    @Test
    void recommend_DedupFiltersExistingNames() {
        List<Destination> dbCandidates = createDestinations("Nha Trang", 2);
        dbCandidates.get(0).setName("Tháp Bà Ponagar");
        dbCandidates.get(1).setName("Vinpearl Land");
        when(destinationRepository.findCandidates(eq("Nha Trang"), any())).thenReturn(dbCandidates);

        when(geocodingClient.geocode("Nha Trang, Vietnam"))
                .thenReturn(new NominatimGeocodingClient.GeocodingResult(12.238, 109.196, "Nha Trang"));

        // External returns 3 results, but 1 already exists in DB (same name, different case)
        List<Destination> externalResults = new ArrayList<>();
        externalResults.add(buildDestination("Nha Trang", "tháp bà ponagar")); // duplicate!
        externalResults.add(buildDestination("Nha Trang", "Hòn Chồng"));       // new
        externalResults.add(buildDestination("Nha Trang", "Chợ Đầm"));         // new
        when(destinationSearchPort.searchNearby(anyDouble(), anyDouble(), anyDouble(), eq("Nha Trang")))
                .thenReturn(externalResults);

        // findByCity returns all DB destinations for dedup
        when(destinationRepository.findByCity("Nha Trang")).thenReturn(new ArrayList<>(dbCandidates));
        when(destinationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        service.recommend(ctx, "Nha Trang");

        // Verify saveAll was called with only 2 new destinations (the duplicate was filtered)
        verify(destinationRepository).saveAll(argThat(list -> {
            @SuppressWarnings("unchecked")
            List<Destination> saved = (List<Destination>) list;
            assertEquals(2, saved.size());
            assertTrue(saved.stream().anyMatch(d -> d.getName().equals("Hòn Chồng")));
            assertTrue(saved.stream().anyMatch(d -> d.getName().equals("Chợ Đầm")));
            return true;
        }));
    }

    /**
     * When geocoding fails, enrichment should be skipped gracefully
     * and the original (small) candidate list should be used.
     */
    @Test
    void recommend_GeocodingFails_SkipsEnrichment() {
        List<Destination> dbCandidates = createDestinations("Unknown City", 1);
        when(destinationRepository.findCandidates(eq("Unknown City"), any())).thenReturn(dbCandidates);

        // Geocoding returns null (city not found)
        when(geocodingClient.geocode("Unknown City, Vietnam")).thenReturn(null);

        service.recommend(ctx, "Unknown City");

        // External search should NOT be called if geocoding fails
        verify(destinationSearchPort, never()).searchNearby(anyDouble(), anyDouble(), anyDouble(), anyString());
        verify(destinationRepository, never()).saveAll(anyList());
    }

    /**
     * When dynamic search is disabled via config, enrichment should never trigger.
     */
    @Test
    void recommend_DynamicSearchDisabled_NeverEnriches() {
        ReflectionTestUtils.setField(service, "dynamicSearchEnabled", false);

        List<Destination> dbCandidates = createDestinations("Cần Thơ", 1);
        when(destinationRepository.findCandidates(eq("Cần Thơ"), any())).thenReturn(dbCandidates);

        service.recommend(ctx, "Cần Thơ");

        verify(geocodingClient, never()).geocode(anyString());
        verify(destinationSearchPort, never()).searchNearby(anyDouble(), anyDouble(), anyDouble(), anyString());
    }

    /**
     * When external search returns empty list, no upsert should happen.
     */
    @Test
    void recommend_ExternalSearchReturnsEmpty_NoUpsert() {
        List<Destination> dbCandidates = createDestinations("Phú Quốc", 2);
        when(destinationRepository.findCandidates(eq("Phú Quốc"), any())).thenReturn(dbCandidates);

        when(geocodingClient.geocode("Phú Quốc, Vietnam"))
                .thenReturn(new NominatimGeocodingClient.GeocodingResult(10.227, 103.967, "Phú Quốc"));

        when(destinationSearchPort.searchNearby(anyDouble(), anyDouble(), anyDouble(), eq("Phú Quốc")))
                .thenReturn(List.of());

        service.recommend(ctx, "Phú Quốc");

        verify(destinationRepository, never()).saveAll(anyList());
    }

    // ---- Helpers ----

    private List<Destination> createDestinations(String city, int count) {
        List<Destination> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(buildDestination(city, "Destination " + city + " " + i));
        }
        return list;
    }

    private Destination buildDestination(String city, String name) {
        return Destination.builder()
                .id(UUID.randomUUID())
                .name(name)
                .city(city)
                .category(DestinationCategory.CAFE)
                .latitude(10.0)
                .longitude(106.0)
                .avgRating(4.0)
                .avgCostPerPerson(new BigDecimal("50000"))
                .build();
    }
}
