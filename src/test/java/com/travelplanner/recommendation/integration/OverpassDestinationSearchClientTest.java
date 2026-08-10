package com.travelplanner.recommendation.integration;

import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.domain.DestinationCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OverpassDestinationSearchClientTest {

    private OverpassDestinationSearchClient client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        client = new OverpassDestinationSearchClient();
        ReflectionTestUtils.setField(client, "apiUrl", "https://overpass-api.de/api/interpreter");
        ReflectionTestUtils.setField(client, "maxResults", 30);
        // Set self-proxy to client itself (cache won't trigger in plain unit test)
        ReflectionTestUtils.setField(client, "self", client);

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testSearchNearby_Success_MapsOsmTagsCorrectly() {
        // Overpass response with multiple POI types to verify OSM tag → category mapping
        String overpassResponse = """
                {
                  "elements": [
                    {
                      "type": "node", "id": 1, "lat": 10.762, "lon": 106.660,
                      "tags": { "name": "Bảo tàng Lịch sử", "tourism": "museum" }
                    },
                    {
                      "type": "node", "id": 2, "lat": 10.770, "lon": 106.670,
                      "tags": { "name": "Quán Cà Phê Sài Gòn", "amenity": "cafe" }
                    },
                    {
                      "type": "node", "id": 3, "lat": 10.780, "lon": 106.680,
                      "tags": { "name": "Công viên Tao Đàn", "leisure": "park" }
                    },
                    {
                      "type": "node", "id": 4, "lat": 10.790, "lon": 106.690,
                      "tags": { "name": "Chùa Vĩnh Nghiêm", "amenity": "place_of_worship" }
                    },
                    {
                      "type": "node", "id": 5, "lat": 10.800, "lon": 106.700,
                      "tags": { "name": "Chợ Bến Thành", "shop": "marketplace" }
                    }
                  ]
                }
                """;

        mockServer.expect(ExpectedCount.once(),
                requestTo(org.hamcrest.Matchers.startsWith("https://overpass-api.de/api/interpreter")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(overpassResponse, MediaType.APPLICATION_JSON));

        List<Destination> results = client.searchNearby(10.762, 106.660, 15, "Hồ Chí Minh");

        assertEquals(5, results.size());

        // Verify OSM tag → DestinationCategory mapping
        assertEquals(DestinationCategory.MUSEUM, results.get(0).getCategory());
        assertEquals("Bảo tàng Lịch sử", results.get(0).getName());

        assertEquals(DestinationCategory.CAFE, results.get(1).getCategory());
        assertEquals("Quán Cà Phê Sài Gòn", results.get(1).getName());

        assertEquals(DestinationCategory.PARK, results.get(2).getCategory());
        assertEquals(DestinationCategory.TEMPLE, results.get(3).getCategory());
        assertEquals(DestinationCategory.MARKET, results.get(4).getCategory());

        // Verify city is set correctly on all results
        results.forEach(d -> assertEquals("Hồ Chí Minh", d.getCity()));

        // Verify indoor heuristic
        assertTrue(results.get(0).isIndoor());   // MUSEUM → indoor
        assertTrue(results.get(1).isIndoor());   // CAFE → indoor
        assertFalse(results.get(2).isIndoor());  // PARK → outdoor

        mockServer.verify();
    }

    @Test
    void testSearchNearby_SkipsUnnamedPois() {
        // Elements without name should be filtered out
        String overpassResponse = """
                {
                  "elements": [
                    {
                      "type": "node", "id": 1, "lat": 10.0, "lon": 106.0,
                      "tags": { "amenity": "cafe" }
                    },
                    {
                      "type": "node", "id": 2, "lat": 10.1, "lon": 106.1,
                      "tags": { "name": "Named Cafe", "amenity": "cafe" }
                    }
                  ]
                }
                """;

        mockServer.expect(ExpectedCount.once(),
                requestTo(org.hamcrest.Matchers.startsWith("https://overpass-api.de/api/interpreter")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(overpassResponse, MediaType.APPLICATION_JSON));

        List<Destination> results = client.searchNearby(10.0, 106.0, 10, "Test City");

        // Only the named POI should be returned
        assertEquals(1, results.size());
        assertEquals("Named Cafe", results.get(0).getName());
        mockServer.verify();
    }

    @Test
    void testSearchNearby_SkipsUnmappedCategories() {
        // Elements with OSM tags that don't match our mapping should be skipped
        String overpassResponse = """
                {
                  "elements": [
                    {
                      "type": "node", "id": 1, "lat": 10.0, "lon": 106.0,
                      "tags": { "name": "Gas Station", "amenity": "fuel" }
                    },
                    {
                      "type": "node", "id": 2, "lat": 10.1, "lon": 106.1,
                      "tags": { "name": "Nice Restaurant", "amenity": "restaurant" }
                    }
                  ]
                }
                """;

        mockServer.expect(ExpectedCount.once(),
                requestTo(org.hamcrest.Matchers.startsWith("https://overpass-api.de/api/interpreter")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(overpassResponse, MediaType.APPLICATION_JSON));

        List<Destination> results = client.searchNearby(10.0, 106.0, 10, "Test");

        // "fuel" is not in our mapping, only "restaurant" should survive
        assertEquals(1, results.size());
        assertEquals(DestinationCategory.RESTAURANT, results.get(0).getCategory());
        mockServer.verify();
    }

    @Test
    void testSearchNearby_ApiError_ReturnsEmptyList() {
        // When Overpass API returns server error, should gracefully return empty list
        mockServer.expect(ExpectedCount.once(),
                requestTo(org.hamcrest.Matchers.startsWith("https://overpass-api.de/api/interpreter")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        List<Destination> results = client.searchNearby(10.0, 106.0, 10, "Error City");

        // Should NOT throw, should return empty list
        assertNotNull(results);
        assertTrue(results.isEmpty());
        mockServer.verify();
    }

    @Test
    void testSearchNearby_EmptyResponse_ReturnsEmptyList() {
        String emptyResponse = """
                { "elements": [] }
                """;

        mockServer.expect(ExpectedCount.once(),
                requestTo(org.hamcrest.Matchers.startsWith("https://overpass-api.de/api/interpreter")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(emptyResponse, MediaType.APPLICATION_JSON));

        List<Destination> results = client.searchNearby(10.0, 106.0, 10, "Empty City");

        assertNotNull(results);
        assertTrue(results.isEmpty());
        mockServer.verify();
    }

    @Test
    void testSearchNearby_DefaultValues() {
        // Verify that Destination entities have sensible defaults
        String overpassResponse = """
                {
                  "elements": [
                    {
                      "type": "node", "id": 1, "lat": 16.463, "lon": 107.590,
                      "tags": { "name": "Đại Nội Huế", "tourism": "attraction" }
                    }
                  ]
                }
                """;

        mockServer.expect(ExpectedCount.once(),
                requestTo(org.hamcrest.Matchers.startsWith("https://overpass-api.de/api/interpreter")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(overpassResponse, MediaType.APPLICATION_JSON));

        List<Destination> results = client.searchNearby(16.463, 107.590, 15, "Huế");

        assertEquals(1, results.size());
        Destination dest = results.get(0);
        assertEquals("Đại Nội Huế", dest.getName());
        assertEquals(DestinationCategory.ENTERTAINMENT, dest.getCategory()); // tourism=attraction
        assertEquals("Huế", dest.getCity());
        assertEquals(4.0, dest.getAvgRating());  // default rating
        assertEquals(0, dest.getReviewCount());
        assertEquals(16.463, dest.getLatitude());
        assertEquals(107.590, dest.getLongitude());
        mockServer.verify();
    }
}
