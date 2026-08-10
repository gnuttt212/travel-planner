package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.domain.Transportation;
import com.travelplanner.planning.domain.TripActivity;
import com.travelplanner.planning.domain.TripDuration;
import com.travelplanner.recommendation.domain.*;
import com.travelplanner.recommendation.integration.GeminiNarrativeService;
import com.travelplanner.recommendation.integration.OpenRouteServiceClient;
import com.travelplanner.recommendation.integration.OsrmRoutingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Characterization test (golden master) for PlanBuilder.
 *
 * Purpose: capture the EXACT current behavior of PlanBuilder.buildVariants()
 * so that any future refactoring (e.g. introducing DTOs to break circular
 * dependency with planning module) can be verified against this baseline.
 *
 * These tests use:
 * - Real SlotAllocator (with mocked ORS client that forces haversine fallback)
 * - Mocked GeminiNarrativeService (returns empty string, same as no-API-key behavior)
 * - Fixed UUID seeds for deterministic destination IDs
 */
class PlanBuilderTest {

    // Fixed UUIDs for deterministic tests
    private static final UUID ID_CAFE     = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ID_MUSEUM   = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ID_RESTO    = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ID_PARK     = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID ID_TEMPLE   = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID ID_MARKET   = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final UUID ID_STREET   = UUID.fromString("00000000-0000-0000-0000-000000000007");

    private PlanBuilder planBuilder;

    @BeforeEach
    void setUp() {
        // Mock ORS client to always throw -> forces haversine fallback in SlotAllocator
        OpenRouteServiceClient mockOrs = mock(OpenRouteServiceClient.class);
        when(mockOrs.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenThrow(new RuntimeException("ORS unavailable - using haversine fallback"));

        OsrmRoutingClient mockOsrm = mock(OsrmRoutingClient.class);
        when(mockOsrm.route(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("OSRM unavailable - using haversine fallback"));

        SlotAllocator realAllocator = new SlotAllocator(mockOrs, mockOsrm);

        // Mock Gemini to return empty string (same behavior as when API key is missing)
        GeminiNarrativeService mockGemini = mock(GeminiNarrativeService.class);
        when(mockGemini.generateNarrative(anyString(), any())).thenReturn("");

        planBuilder = new PlanBuilder(realAllocator, mockGemini);
    }

    // =========================================================================
    // Shared destination fixtures - nearby locations in HCM city center
    // =========================================================================

    /** Cafe at Ben Thanh area */
    private Destination cafe() {
        return Destination.builder()
                .id(ID_CAFE).name("Cafe Central")
                .category(DestinationCategory.CAFE)
                .latitude(10.7725).longitude(106.6980)
                .avgRating(4.2).reviewCount(200)
                .avgCostPerPerson(BigDecimal.valueOf(50000))
                .imageUrl("cafe.jpg")
                .build();
    }

    /** Museum ~0.5km from cafe */
    private Destination museum() {
        return Destination.builder()
                .id(ID_MUSEUM).name("War Museum")
                .category(DestinationCategory.MUSEUM)
                .latitude(10.7795).longitude(106.6922)
                .avgRating(4.6).reviewCount(5000)
                .avgCostPerPerson(BigDecimal.valueOf(40000))
                .imageUrl("museum.jpg")
                .build();
    }

    /** Restaurant ~1km from cafe (lunch slot candidate) */
    private Destination restaurant() {
        return Destination.builder()
                .id(ID_RESTO).name("Pho 24")
                .category(DestinationCategory.RESTAURANT)
                .latitude(10.7750).longitude(106.7010)
                .avgRating(4.0).reviewCount(800)
                .avgCostPerPerson(BigDecimal.valueOf(80000))
                .imageUrl("pho.jpg")
                .build();
    }

    /** Park ~1.5km from cafe */
    private Destination park() {
        return Destination.builder()
                .id(ID_PARK).name("Tao Dan Park")
                .category(DestinationCategory.PARK)
                .latitude(10.7740).longitude(106.6910)
                .avgRating(4.3).reviewCount(1200)
                .avgCostPerPerson(BigDecimal.valueOf(0))
                .imageUrl("park.jpg")
                .build();
    }

    /** Temple ~2km from cafe */
    private Destination temple() {
        return Destination.builder()
                .id(ID_TEMPLE).name("Jade Emperor Pagoda")
                .category(DestinationCategory.TEMPLE)
                .latitude(10.7910).longitude(106.6890)
                .avgRating(4.5).reviewCount(3000)
                .avgCostPerPerson(BigDecimal.valueOf(10000))
                .imageUrl("temple.jpg")
                .build();
    }

    /** Market ~0.3km from cafe (food category for food-focused variant) */
    private Destination market() {
        return Destination.builder()
                .id(ID_MARKET).name("Ben Thanh Market")
                .category(DestinationCategory.MARKET)
                .latitude(10.7720).longitude(106.6980)
                .avgRating(4.1).reviewCount(10000)
                .avgCostPerPerson(BigDecimal.valueOf(30000))
                .imageUrl("market.jpg")
                .build();
    }

    /** Street food ~0.8km from cafe (food category for food-focused & lunch slot) */
    private Destination streetFood() {
        return Destination.builder()
                .id(ID_STREET).name("Banh Mi Street")
                .category(DestinationCategory.STREET_FOOD)
                .latitude(10.7760).longitude(106.7000)
                .avgRating(4.4).reviewCount(600)
                .avgCostPerPerson(BigDecimal.valueOf(25000))
                .imageUrl("banhmi.jpg")
                .build();
    }

    // =========================================================================
    // Helper: create ScoredDestination with a dummy CompositeScore
    // The total score determines ordering (descending) in the default "balanced" sort
    // =========================================================================

    private ScoredDestination scored(Destination d, double total) {
        return new ScoredDestination(d, new CompositeScore(total, 0.8, 0.9, 0.7, 0.6, 0.8));
    }

    // =========================================================================
    // CASE 1: FULL_DAY with many destinations — the primary golden master
    // =========================================================================

    @Test
    @DisplayName("Golden Master: FULL_DAY with 7 destinations - captures variant structure and activity ordering")
    void fullDay_7Destinations_capturesBehavior() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.7725).startLon(106.6980)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.of(2024, 1, 15)) // Monday
                .duration(TripDuration.FULL_DAY)
                .budgetPerPerson(BigDecimal.valueOf(500000))
                .styles(List.of("Food", "Culture"))
                .build();

        // Scored in descending total score order:
        // museum(0.95) > temple(0.90) > streetFood(0.85) > park(0.80) > cafe(0.75) > restaurant(0.70) > market(0.65)
        List<ScoredDestination> allScored = List.of(
                scored(museum(), 0.95),
                scored(temple(), 0.90),
                scored(streetFood(), 0.85),
                scored(park(), 0.80),
                scored(cafe(), 0.75),
                scored(restaurant(), 0.70),
                scored(market(), 0.65)
        );

        List<TripPlanVariant> variants = planBuilder.buildVariants(allScored, ctx);

        // === Structure assertions ===
        assertEquals(3, variants.size(), "Should produce exactly 3 variants");
        // Variant names (Vietnamese, from PlanBuilder)
        assertEquals("Cân bằng", variants.get(0).name());
        assertEquals("Ẩm thực", variants.get(1).name());
        assertEquals("Tiết kiệm", variants.get(2).name());
        // AI narrative is empty since Gemini is mocked
        assertEquals("", variants.get(0).aiNarrative());

        // === Variant A: Balanced (original scored order) ===
        List<TripActivity> balanced = variants.get(0).activities();
        assertFalse(balanced.isEmpty(), "Balanced variant should have activities");
        // First activity should be museum (highest score)
        assertEquals(ID_MUSEUM, balanced.get(0).getDestinationId());
        assertEquals("War Museum", balanced.get(0).getDestinationName());
        assertEquals("MUSEUM", balanced.get(0).getDestinationCategory());
        assertEquals(0, balanced.get(0).getOrderIndex());
        assertEquals(LocalTime.of(9, 5), balanced.get(0).getPlannedStartTime());
        // Includes initial travel from the trip start point, then museum duration = 105 min.
        assertEquals(LocalTime.of(10, 50), balanced.get(0).getPlannedEndTime());
        assertEquals(105, balanced.get(0).getEstimatedDurationMinutes());

        // Verify total cost = sum of estimatedCost of all activities
        BigDecimal expectedBalancedCost = balanced.stream()
                .map(a -> a.getEstimatedCost() != null ? a.getEstimatedCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, expectedBalancedCost.compareTo(variants.get(0).totalCost()),
                "Balanced totalCost should match sum of activity costs");

        // Verify total distance = sum of travelDistanceKm
        double expectedBalancedDist = balanced.stream().mapToDouble(TripActivity::getTravelDistanceKm).sum();
        assertEquals(expectedBalancedDist, variants.get(0).totalDistanceKm(), 0.001,
                "Balanced totalDistanceKm should match sum of travel distances");

        // Verify orderIndex is sequential
        for (int i = 0; i < balanced.size(); i++) {
            assertEquals(i, balanced.get(i).getOrderIndex(), "orderIndex should be sequential");
        }

        // Verify all activities have UPCOMING status
        balanced.forEach(a -> assertEquals("UPCOMING", a.getStatus().name()));

        // === Variant B: Food-focused (food categories sorted first) ===
        List<TripActivity> foodFocused = variants.get(1).activities();
        assertFalse(foodFocused.isEmpty(), "Food-focused variant should have activities");
        // Food categories: RESTAURANT, STREET_FOOD, CAFE, MARKET should be sorted to front
        // First activity in food-focused should be one of the food categories
        String firstFoodCat = foodFocused.get(0).getDestinationCategory();
        assertTrue(
                List.of("RESTAURANT", "STREET_FOOD", "CAFE", "MARKET").contains(firstFoodCat),
                "Food-focused first activity should be a food category, got: " + firstFoodCat
        );

        // === Variant C: Budget-optimized (sorted by avgCostPerPerson ascending) ===
        List<TripActivity> budgetOpt = variants.get(2).activities();
        assertFalse(budgetOpt.isEmpty(), "Budget variant should have activities");
        // First activity should be the cheapest destination (park at 0 cost)
        assertEquals(ID_PARK, budgetOpt.get(0).getDestinationId(),
                "Budget variant first activity should be the cheapest destination (Tao Dan Park, cost=0)");
    }

    // =========================================================================
    // CASE 2: HALF_DAY with few destinations — max 3 activities
    // =========================================================================

    @Test
    @DisplayName("Golden Master: HALF_DAY with 5 destinations - max 3 activities, ends by 13:00")
    void halfDay_5Destinations_max3Activities() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.7725).startLon(106.6980)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.of(2024, 1, 15)) // Monday
                .duration(TripDuration.HALF_DAY)
                .budgetPerPerson(BigDecimal.valueOf(200000))
                .styles(List.of("Culture"))
                .build();

        List<ScoredDestination> allScored = List.of(
                scored(cafe(), 0.90),
                scored(museum(), 0.85),
                scored(restaurant(), 0.80),
                scored(park(), 0.75),
                scored(temple(), 0.70)
        );

        List<TripPlanVariant> variants = planBuilder.buildVariants(allScored, ctx);

        assertEquals(3, variants.size());

        // All 3 variants should have <= 3 activities (HALF_DAY max)
        for (TripPlanVariant v : variants) {
            assertTrue(v.activities().size() <= 3,
                    "HALF_DAY variant '" + v.name() + "' should have at most 3 activities, got " + v.activities().size());
        }

        // All activities should end by 13:00 (HALF_DAY endTime)
        for (TripPlanVariant v : variants) {
            for (TripActivity a : v.activities()) {
                assertTrue(!a.getPlannedEndTime().isAfter(LocalTime.of(13, 0)),
                        "HALF_DAY activity should end by 13:00, but " + a.getDestinationName() +
                                " ends at " + a.getPlannedEndTime() + " in variant " + v.name());
            }
        }

        // Balanced first activity: cafe (highest score)
        List<TripActivity> balanced = variants.get(0).activities();
        assertEquals(ID_CAFE, balanced.get(0).getDestinationId());
        assertEquals(LocalTime.of(9, 0), balanced.get(0).getPlannedStartTime());
        // Cafe duration = 75 min -> ends 10:15
        assertEquals(LocalTime.of(10, 15), balanced.get(0).getPlannedEndTime());
    }

    // =========================================================================
    // CASE 3: Low budget scenario — verifies budget variant ordering
    // =========================================================================

    @Test
    @DisplayName("Golden Master: Budget variant always sorts cheapest first regardless of score")
    void budgetVariant_sortsBy_costAscending() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.7725).startLon(106.6980)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.of(2024, 1, 15))
                .duration(TripDuration.FULL_DAY)
                .budgetPerPerson(BigDecimal.valueOf(100000))
                .styles(List.of("Nature"))
                .build();

        // Destinations with varying costs: park(0) < temple(10k) < streetFood(25k) < market(30k) < museum(40k) < cafe(50k) < restaurant(80k)
        // Scores are INVERSE to cost: restaurant has highest score but highest cost
        List<ScoredDestination> allScored = List.of(
                scored(restaurant(), 0.98),
                scored(cafe(), 0.90),
                scored(museum(), 0.85),
                scored(market(), 0.80),
                scored(streetFood(), 0.75),
                scored(temple(), 0.70),
                scored(park(), 0.65)
        );

        List<TripPlanVariant> variants = planBuilder.buildVariants(allScored, ctx);

        // Budget variant (index 2)
        List<TripActivity> budgetActivities = variants.get(2).activities();
        assertFalse(budgetActivities.isEmpty());

        // First activity in budget variant should be cheapest: Park (cost = 0)
        assertEquals(ID_PARK, budgetActivities.get(0).getDestinationId(),
                "Budget variant should start with cheapest destination (park, cost=0)");
        assertEquals(BigDecimal.valueOf(0), budgetActivities.get(0).getEstimatedCost());

        // Budget variant totalCost should be <= balanced variant totalCost
        BigDecimal budgetCost = variants.get(2).totalCost();
        BigDecimal balancedCost = variants.get(0).totalCost();
        assertTrue(budgetCost.compareTo(balancedCost) <= 0,
                "Budget variant cost (" + budgetCost + ") should be <= balanced variant cost (" + balancedCost + ")");

        // Verify the balanced variant still uses scored order (restaurant first, highest score)
        assertEquals(ID_RESTO, variants.get(0).activities().get(0).getDestinationId(),
                "Balanced variant should start with highest-scored destination (restaurant)");
    }
}
