package com.travelplanner.recommendation.engine;

import com.travelplanner.planning.domain.Transportation;
import com.travelplanner.planning.domain.TravelContext;
import com.travelplanner.planning.domain.TripDuration;
import com.travelplanner.recommendation.domain.CompositeScore;
import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.domain.DestinationCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScoringEngineTest {

    private ScoringEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ScoringEngine();
    }

    @Test
    void testScoreDistance_NearbyLocation_HighDistanceScore() {
        // Create context at Ben Thanh Market (lat: 10.7725, lon: 106.6980)
        TravelContext ctx = TravelContext.builder()
                .startLat(10.7725)
                .startLon(106.6980)
                .transportation(Transportation.MOTORBIKE) // Max radius 30km
                .date(LocalDate.of(2023, 10, 20)) // Friday
                .budgetPerPerson(BigDecimal.valueOf(500000))
                .styles(List.of("Food", "Culture"))
                .duration(TripDuration.FULL_DAY)
                .build();

        // Destination: Independence Palace (lat: 10.7770, lon: 106.6954) -> Very close
        Destination dest = Destination.builder()
                .id(UUID.randomUUID())
                .name("Independence Palace")
                .latitude(10.7770)
                .longitude(106.6954)
                .category(DestinationCategory.MUSEUM)
                .avgRating(4.5)
                .reviewCount(1000)
                .avgCostPerPerson(BigDecimal.valueOf(40000))
                .tags("[\"Culture\", \"History\"]")
                .openingHours("{\"friday\": \"08:00-16:00\"}")
                .build();

        CompositeScore score = engine.score(dest, ctx, false);
        
        assertTrue(score.distanceScore() > 0.9, "Distance score should be very high for nearby locations");
    }

    @Test
    void testScoreDistance_FarLocation_LowerDistanceScore() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.7725)
                .startLon(106.6980)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.now())
                .build();

        // Destination: Cu Chi Tunnels (lat: 11.1420, lon: 106.4620) -> ~50km away
        Destination dest = Destination.builder()
                .id(UUID.randomUUID())
                .name("Cu Chi Tunnels")
                .latitude(11.1420)
                .longitude(106.4620)
                .category(DestinationCategory.MUSEUM)
                .avgRating(4.5)
                .reviewCount(1000)
                .build();

        CompositeScore score = engine.score(dest, ctx, false);
        
        assertTrue(score.distanceScore() < 0.1, "Should have a very low score since it's far");
    }

    @Test
    void testScoreBudget_WithinBudget_HighScore() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.0)
                .startLon(106.0)
                .transportation(Transportation.MOTORBIKE)
                .budgetPerPerson(BigDecimal.valueOf(100000))
                .date(LocalDate.now())
                .build();

        Destination dest = Destination.builder()
                .id(UUID.randomUUID())
                .avgCostPerPerson(BigDecimal.valueOf(50000)) // 50k < 100k
                .build();

        CompositeScore score = engine.score(dest, ctx, false);
        assertEquals(1.0, score.budgetScore(), 0.01);
    }

    @Test
    void testScoreBudget_OverBudget_LowScore() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.0)
                .startLon(106.0)
                .transportation(Transportation.MOTORBIKE)
                .budgetPerPerson(BigDecimal.valueOf(100000))
                .date(LocalDate.now())
                .build();

        Destination dest = Destination.builder()
                .id(UUID.randomUUID())
                .avgCostPerPerson(BigDecimal.valueOf(150000)) // 150k > 100k (ratio 1.5)
                .build();

        CompositeScore score = engine.score(dest, ctx, false);
        assertEquals(0.0, score.budgetScore(), 0.01, "Should be 0 if way over budget");
    }

    @Test
    void testScorePreference_MatchingTags() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.0)
                .startLon(106.0)
                .transportation(Transportation.MOTORBIKE)
                .styles(List.of("Nature", "Chill"))
                .date(LocalDate.now())
                .build();

        Destination dest = Destination.builder()
                .id(UUID.randomUUID())
                .tags("[\"Nature\", \"Photography\"]") // 1 match out of 2 styles
                .build();

        CompositeScore score = engine.score(dest, ctx, false);
        assertEquals(0.5, score.preferenceScore(), 0.01); // 1 / 2 = 0.5
    }

    @Test
    void testScoreHours_OpenAndClosed() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.0)
                .startLon(106.0)
                .transportation(Transportation.MOTORBIKE)
                .date(LocalDate.of(2024, 1, 1)) // Monday
                .build();

        // Closed on Monday (short name is 'mon')
        Destination destClosed = Destination.builder()
                .id(UUID.randomUUID())
                .openingHours("{\"mon\": \"closed\"}")
                .build();
        
        CompositeScore scoreClosed = engine.score(destClosed, ctx, false);
        assertEquals(0.0, scoreClosed.hoursScore(), 0.01);

        // Open 24h
        Destination dest24h = Destination.builder()
                .id(UUID.randomUUID())
                .openingHours("{\"mon\": \"24h\"}")
                .build();
        
        CompositeScore score24h = engine.score(dest24h, ctx, false);
        assertEquals(1.0, score24h.hoursScore(), 0.01);
    }

    @Test
    void testScoreWithWeatherAdjustment() {
        Destination outdoorDest = Destination.builder()
            .name("Outdoor Park")
            .latitude(10.01)
            .longitude(106.01)
            .indoor(false)
            .avgRating(4.5)
            .reviewCount(100)
            .build();
            
        Destination indoorDest = Destination.builder()
            .name("Indoor Museum")
            .latitude(10.01)
            .longitude(106.01)
            .indoor(true)
            .avgRating(4.5)
            .reviewCount(100)
            .build();

        TravelContext ctx = TravelContext.builder()
            .date(LocalDate.of(2023, 10, 25))
            .startLat(10.0)
            .startLon(106.0)
            .transportation(Transportation.CAR)
            .build();

        CompositeScore outdoorScoreRain = engine.score(outdoorDest, ctx, true);
        CompositeScore indoorScoreRain = engine.score(indoorDest, ctx, true);
        
        CompositeScore outdoorScoreNoRain = engine.score(outdoorDest, ctx, false);
        CompositeScore indoorScoreNoRain = engine.score(indoorDest, ctx, false);

        // Outdoor should be penalized when raining
        assertTrue(outdoorScoreRain.total() < outdoorScoreNoRain.total());
        
        // Indoor should be boosted when raining
        assertTrue(indoorScoreRain.total() > indoorScoreNoRain.total());
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Địa điểm rating cao nhưng review ít (dưới 5) phải bị phạt điểm tin cậy")
    void testReliability_HighRating_LowReviews_ShouldBePenalized() {
        TravelContext ctx = TravelContext.builder().startLat(10.0).startLon(106.0).date(LocalDate.now()).build();
        
        // 5.0 sao nhưng chỉ có 2 reviews -> phải bị phạt 50%
        Destination destLowReview = Destination.builder()
                .id(UUID.randomUUID())
                .avgRating(5.0)
                .reviewCount(2)
                .build();
                
        // 4.5 sao nhưng có 1000 reviews -> không bị phạt
        Destination destHighReview = Destination.builder()
                .id(UUID.randomUUID())
                .avgRating(4.5)
                .reviewCount(1000)
                .build();
                
        CompositeScore scoreLow = engine.score(destLowReview, ctx, false);
        CompositeScore scoreHigh = engine.score(destHighReview, ctx, false);
        
        // Dù rating cao hơn, nhưng ít review nên điểm reliability cuối cùng phải thấp hơn
        assertTrue(scoreLow.ratingScore() < scoreHigh.ratingScore(), 
            "Địa điểm ít review phải có điểm tin cậy thấp hơn địa điểm nhiều review");
    }
    
    @Test
    @org.junit.jupiter.api.DisplayName("Trường hợp không có review nào (reviewCount = 0) không được ném lỗi chia cho 0")
    void testReliability_ZeroReviews_ShouldNotThrowException() {
        TravelContext ctx = TravelContext.builder().startLat(10.0).startLon(106.0).date(LocalDate.now()).build();
        
        Destination destNoReview = Destination.builder()
                .id(UUID.randomUUID())
                .avgRating(0.0)
                .reviewCount(0)
                .build();
                
        assertDoesNotThrow(() -> {
            CompositeScore score = engine.score(destNoReview, ctx, false);
            assertTrue(score.ratingScore() >= 0.0);
        });
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Địa điểm rating trung bình nhưng review nhiều phải được ưu tiên hơn rating cao mà review mỏng")
    void testReliability_AverageRating_HighReviews_Vs_HighRating_LowReviews() {
        TravelContext ctx = TravelContext.builder().startLat(10.0).startLon(106.0).date(LocalDate.now()).build();
        
        // 4.0 sao, 2000 reviews
        Destination destAvgRatingHighReview = Destination.builder()
                .id(UUID.randomUUID())
                .avgRating(4.0)
                .reviewCount(2000)
                .build();
                
        // 4.8 sao, 10 reviews -> Bị phạt 20%
        Destination destHighRatingLowReview = Destination.builder()
                .id(UUID.randomUUID())
                .avgRating(4.8)
                .reviewCount(10)
                .build();
                
        CompositeScore score1 = engine.score(destAvgRatingHighReview, ctx, false);
        CompositeScore score2 = engine.score(destHighRatingLowReview, ctx, false);
        
        assertTrue(score1.ratingScore() > score2.ratingScore(), 
            "4.0 sao / 2000 review phải đáng tin cậy hơn 4.8 sao / 10 review");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Ưu tiên theo nhóm: Đi với gia đình (FAMILY) phải ưu tiên chỗ indoor, kid-friendly và phạt BAR")
    void testPreference_FamilyGroup() {
        TravelContext ctx = TravelContext.builder()
                .startLat(10.0).startLon(106.0).date(LocalDate.now())
                .groupType("FAMILY")
                .build();

        Destination familyFriendly = Destination.builder()
                .id(UUID.randomUUID())
                .indoor(true)
                .tags("[\"kid-friendly\", \"spacious\"]")
                .category(DestinationCategory.RESTAURANT)
                .build();

        Destination barDest = Destination.builder()
                .id(UUID.randomUUID())
                .indoor(false)
                .category(DestinationCategory.BAR)
                .tags("[\"loud\"]")
                .build();

        CompositeScore familyScore = engine.score(familyFriendly, ctx, false);
        CompositeScore barScore = engine.score(barDest, ctx, false);

        assertTrue(familyScore.preferenceScore() > barScore.preferenceScore(),
                "Chỗ chơi gia đình phải có điểm sở thích cao hơn BAR");
    }
}
