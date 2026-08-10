package com.travelplanner.recommendation.service;

import com.travelplanner.common.dto.RecommendationRequest;
import com.travelplanner.common.dto.RecommendationVariantResponse;
import com.travelplanner.planning.domain.Transportation;
import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.domain.TripDuration;
import com.travelplanner.recommendation.domain.CompositeScore;
import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.domain.DestinationCategory;
import com.travelplanner.recommendation.domain.ScoredDestination;
import com.travelplanner.recommendation.domain.TripPlanVariant;
import com.travelplanner.recommendation.engine.PlanBuilder;
import com.travelplanner.recommendation.engine.SlotAllocator;
import com.travelplanner.recommendation.integration.GeminiNarrativeService;
import com.travelplanner.recommendation.integration.OpenRouteServiceClient;
import com.travelplanner.recommendation.integration.OsrmRoutingClient;
import com.travelplanner.recommendation.integration.OpenWeatherMapServiceClient;
import com.travelplanner.recommendation.repository.DestinationRepository;
import com.travelplanner.recommendation.engine.ScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationPortImplTest {

    // Fixed UUIDs for deterministic tests
    private static final UUID ID_CAFE     = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ID_MUSEUM   = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ID_RESTO    = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ID_PARK     = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID ID_TEMPLE   = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID ID_MARKET   = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final UUID ID_STREET   = UUID.fromString("00000000-0000-0000-0000-000000000007");

    private PlanBuilder realPlanBuilder;
    private RecommendationPortImpl recommendationPort;

    @BeforeEach
    void setUp() {
        // 1. Setup PlanBuilder identical to PlanBuilderTest
        OpenRouteServiceClient mockOrs = mock(OpenRouteServiceClient.class);
        when(mockOrs.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenThrow(new RuntimeException("ORS unavailable - using haversine fallback"));

        OsrmRoutingClient mockOsrm = mock(OsrmRoutingClient.class);
        when(mockOsrm.route(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("OSRM unavailable - using haversine fallback"));

        SlotAllocator realAllocator = new SlotAllocator(mockOrs, mockOsrm);

        GeminiNarrativeService mockGemini = mock(GeminiNarrativeService.class);
        when(mockGemini.generateNarrative(anyString(), any())).thenReturn("");

        realPlanBuilder = new PlanBuilder(realAllocator, mockGemini);

        // 2. Mock RecommendationService dependencies to inject our realPlanBuilder
        DestinationRepository mockDestRepo = mock(DestinationRepository.class);
        ScoringEngine mockScoringEngine = mock(ScoringEngine.class);
        OpenWeatherMapServiceClient mockWeather = mock(OpenWeatherMapServiceClient.class);
        com.travelplanner.recommendation.port.DestinationSearchPort mockSearchPort =
                mock(com.travelplanner.recommendation.port.DestinationSearchPort.class);
        com.travelplanner.recommendation.integration.NominatimGeocodingClient mockGeocoding =
                mock(com.travelplanner.recommendation.integration.NominatimGeocodingClient.class);
        
        // Ensure no rain
        when(mockWeather.isRaining(anyDouble(), anyDouble(), any())).thenReturn(false);

        // The scored destinations from PlanBuilderTest (7 candidates >= threshold, so no enrichment)
        List<Destination> candidates = List.of(museum(), temple(), streetFood(), park(), cafe(), restaurant(), market());
        when(mockDestRepo.findCandidates(eq("HCM"), any())).thenReturn(candidates);

        // Mock ScoringEngine to return the exact scores we used in PlanBuilderTest
        mockScore(mockScoringEngine, museum(), 0.95);
        mockScore(mockScoringEngine, temple(), 0.90);
        mockScore(mockScoringEngine, streetFood(), 0.85);
        mockScore(mockScoringEngine, park(), 0.80);
        mockScore(mockScoringEngine, cafe(), 0.75);
        mockScore(mockScoringEngine, restaurant(), 0.70);
        mockScore(mockScoringEngine, market(), 0.65);

        RecommendationService recService = new RecommendationService(mockDestRepo, mockScoringEngine, realPlanBuilder, mockWeather, mockSearchPort, mockGeocoding);
        
        RecommendationMapper mapper = new RecommendationMapper();
        recommendationPort = new RecommendationPortImpl(recService, mapper);
    }
    
    private void mockScore(ScoringEngine mockEngine, Destination dest, double score) {
        when(mockEngine.score(eq(dest), any(TravelContext.class), anyBoolean()))
            .thenReturn(new CompositeScore(score, 0.8, 0.9, 0.7, 0.6, 0.8));
    }

    // =========================================================================
    // Shared destination fixtures (same as PlanBuilderTest)
    // =========================================================================

    private Destination cafe() { return Destination.builder().id(ID_CAFE).name("Cafe Central").category(DestinationCategory.CAFE).latitude(10.7725).longitude(106.6980).avgRating(4.2).reviewCount(200).avgCostPerPerson(BigDecimal.valueOf(50000)).imageUrl("cafe.jpg").build(); }
    private Destination museum() { return Destination.builder().id(ID_MUSEUM).name("War Museum").category(DestinationCategory.MUSEUM).latitude(10.7795).longitude(106.6922).avgRating(4.6).reviewCount(5000).avgCostPerPerson(BigDecimal.valueOf(40000)).imageUrl("museum.jpg").build(); }
    private Destination restaurant() { return Destination.builder().id(ID_RESTO).name("Pho 24").category(DestinationCategory.RESTAURANT).latitude(10.7750).longitude(106.7010).avgRating(4.0).reviewCount(800).avgCostPerPerson(BigDecimal.valueOf(80000)).imageUrl("pho.jpg").build(); }
    private Destination park() { return Destination.builder().id(ID_PARK).name("Tao Dan Park").category(DestinationCategory.PARK).latitude(10.7740).longitude(106.6910).avgRating(4.3).reviewCount(1200).avgCostPerPerson(BigDecimal.valueOf(0)).imageUrl("park.jpg").build(); }
    private Destination temple() { return Destination.builder().id(ID_TEMPLE).name("Jade Emperor Pagoda").category(DestinationCategory.TEMPLE).latitude(10.7910).longitude(106.6890).avgRating(4.5).reviewCount(3000).avgCostPerPerson(BigDecimal.valueOf(10000)).imageUrl("temple.jpg").build(); }
    private Destination market() { return Destination.builder().id(ID_MARKET).name("Ben Thanh Market").category(DestinationCategory.MARKET).latitude(10.7720).longitude(106.6980).avgRating(4.1).reviewCount(10000).avgCostPerPerson(BigDecimal.valueOf(30000)).imageUrl("market.jpg").build(); }
    private Destination streetFood() { return Destination.builder().id(ID_STREET).name("Banh Mi Street").category(DestinationCategory.STREET_FOOD).latitude(10.7760).longitude(106.7000).avgRating(4.4).reviewCount(600).avgCostPerPerson(BigDecimal.valueOf(25000)).imageUrl("banhmi.jpg").build(); }

    private ScoredDestination scored(Destination d, double total) {
        return new ScoredDestination(d, new CompositeScore(total, 0.8, 0.9, 0.7, 0.6, 0.8));
    }

    @Test
    @DisplayName("Verify DTO path (RecommendationPort) produces EXACTLY same output as old path (PlanBuilder)")
    void compareNewPathWithOldPath() {
        // 1. Inputs
        LocalDate date = LocalDate.of(2024, 1, 15);
        BigDecimal budget = BigDecimal.valueOf(500000);
        List<String> styles = List.of("Food", "Culture");

        // Old context
        TravelContext ctx = TravelContext.builder()
                .startLat(10.7725).startLon(106.6980)
                .transportation(Transportation.MOTORBIKE)
                .date(date)
                .duration(TripDuration.FULL_DAY)
                .budgetPerPerson(budget)
                .styles(styles)
                .build();
                
        // Scored in descending total score order for old path
        List<ScoredDestination> allScored = List.of(
                scored(museum(), 0.95),
                scored(temple(), 0.90),
                scored(streetFood(), 0.85),
                scored(park(), 0.80),
                scored(cafe(), 0.75),
                scored(restaurant(), 0.70),
                scored(market(), 0.65)
        );

        // New request
        RecommendationRequest request = RecommendationRequest.builder()
                .city("HCM")
                .startLat(10.7725).startLon(106.6980)
                .transportationType("MOTORBIKE")
                .date(date)
                .durationType("FULL_DAY")
                .budgetPerPerson(budget)
                .styles(styles)
                .build();

        // 2. Execute both paths
        List<TripPlanVariant> oldVariants = realPlanBuilder.buildVariants(allScored, ctx);
        List<RecommendationVariantResponse> newVariants = recommendationPort.recommend(request);

        // 3. Compare top level variants
        assertEquals(oldVariants.size(), newVariants.size(), "Number of variants should match");
        
        for (int i = 0; i < oldVariants.size(); i++) {
            TripPlanVariant oldVar = oldVariants.get(i);
            RecommendationVariantResponse newVar = newVariants.get(i);
            
            assertEquals(oldVar.name(), newVar.getVariantName());
            assertEquals(oldVar.description(), newVar.getVariantDescription());
            assertEquals(0, oldVar.totalCost().compareTo(newVar.getTotalCost()));
            assertEquals(oldVar.totalDistanceKm(), newVar.getTotalDistanceKm(), 0.001, "Khoảng cách tổng phải khớp");
            
            // Compare activities
            assertEquals(oldVar.activities().size(), newVar.getActivities().size(), "Số lượng activity phải khớp giữa 2 luồng");
            for (int j = 0; j < oldVar.activities().size(); j++) {
                var oldAct = oldVar.activities().get(j);
                var newAct = newVar.getActivities().get(j);
                
                assertEquals(oldAct.getDestinationId(), newAct.getDestinationId());
                assertEquals(oldAct.getDestinationName(), newAct.getDestinationName());
                assertEquals(oldAct.getPlannedStartTime(), newAct.getPlannedStartTime());
                assertEquals(oldAct.getPlannedEndTime(), newAct.getPlannedEndTime());
                assertEquals(oldAct.getOrderIndex(), newAct.getOrderIndex());
                assertEquals(oldAct.getEstimatedCost(), newAct.getEstimatedCost());
            }
        }
    }
}
