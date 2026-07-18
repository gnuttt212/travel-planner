package com.travelplanner.itinerary.mapper;

import com.travelplanner.itinerary.domain.Destination;
import com.travelplanner.itinerary.dto.DestinationRequest;
import com.travelplanner.itinerary.dto.DestinationResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DestinationMapperTest {

    private final DestinationMapper mapper = new DestinationMapper();

    @Test
    void shouldMapRequestToEntity() {
        DestinationRequest request = new DestinationRequest(
                "Paris",
                "France",
                "Lovely city",
                List.of("food", "culture"),
                List.of(5, 6),
                java.math.BigDecimal.valueOf(120.0)
        );

        Destination destination = mapper.toEntity(request);

        assertNotNull(destination);
        assertEquals("Paris", destination.getName());
        assertEquals("France", destination.getCountry());
        assertEquals("Lovely city", destination.getDescription());
        assertEquals(List.of("food", "culture"), destination.getTags());
        assertEquals(List.of(5, 6), destination.getBestMonths());
        assertEquals(120.0, destination.getAvgCostPerDay().doubleValue());
    }

    @Test
    void shouldMapEntityToResponse() {
        Destination destination = Destination.builder()
                .name("Tokyo")
                .country("Japan")
                .description("Busy city")
                .tags(List.of("nightlife"))
                .bestMonths(List.of(4))
                .avgCostPerDay(java.math.BigDecimal.valueOf(180.0))
                .popularityScore(91.5)
                .latitude(35.6764)
                .longitude(139.6503)
                .build();

        DestinationResponse response = mapper.toResponse(destination);

        assertNotNull(response);
        assertEquals(null, response.id());
        assertEquals("Tokyo", response.name());
        assertEquals("Japan", response.country());
        assertEquals("Busy city", response.description());
        assertEquals(List.of("nightlife"), response.tags());
        assertEquals(List.of(4), response.bestMonths());
        assertEquals(180.0, response.avgCostPerDay().doubleValue());
        assertEquals(91.5, response.popularityScore());
        assertEquals(35.6764, response.latitude());
        assertEquals(139.6503, response.longitude());
    }
}
