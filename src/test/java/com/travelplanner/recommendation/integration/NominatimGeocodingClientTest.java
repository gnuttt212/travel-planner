package com.travelplanner.recommendation.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = NominatimGeocodingClientTest.TestConfig.class)
@TestPropertySource(properties = {
    "nominatim.api.url=https://nominatim.openstreetmap.org/search"
})
class NominatimGeocodingClientTest {

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        public NominatimGeocodingClient nominatimGeocodingClient() {
            return new NominatimGeocodingClient();
        }
        
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("geocodingCache");
        }
    }

    @Autowired
    private NominatimGeocodingClient client;

    @Autowired
    private CacheManager cacheManager;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("geocodingCache").clear();
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testGeocode_IsCached() {
        String validJsonResponse = "[{\"lat\":\"10.0\",\"lon\":\"106.0\",\"display_name\":\"Test Place\"}]";
        
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://nominatim.openstreetmap.org/search")))
                .andRespond(withSuccess(validJsonResponse, org.springframework.http.MediaType.APPLICATION_JSON));

        NominatimGeocodingClient.GeocodingResult r1 = client.geocode("Test Place");
        assertEquals(10.0, r1.lat());

        // Call again - should hit cache
        NominatimGeocodingClient.GeocodingResult r2 = client.geocode("Test Place");
        assertEquals(10.0, r2.lat());

        mockServer.verify();
    }
    
    @Test
    void testGeocode_NullNotCached() {
        String emptyJsonResponse = "[]"; // Nominatim returns empty array when not found
        
        mockServer.expect(ExpectedCount.times(2), 
                requestTo(org.hamcrest.Matchers.startsWith("https://nominatim.openstreetmap.org/search")))
                .andRespond(withSuccess(emptyJsonResponse, org.springframework.http.MediaType.APPLICATION_JSON));

        // First call - should return null
        NominatimGeocodingClient.GeocodingResult r1 = client.geocode("NonExistentPlace123");
        org.junit.jupiter.api.Assertions.assertNull(r1);

        // Second call - should hit the API AGAIN because nulls are NOT cached
        NominatimGeocodingClient.GeocodingResult r2 = client.geocode("NonExistentPlace123");
        org.junit.jupiter.api.Assertions.assertNull(r2);

        mockServer.verify();
    }
    
    @Test
    void testGeocode_RateLimiter() {
        String validJsonResponse = "[{\"lat\":\"10.0\",\"lon\":\"106.0\",\"display_name\":\"Test\"}]";
        
        mockServer.expect(ExpectedCount.times(3), 
                requestTo(org.hamcrest.Matchers.startsWith("https://nominatim.openstreetmap.org/search")))
                .andRespond(withSuccess(validJsonResponse, org.springframework.http.MediaType.APPLICATION_JSON));

        long start = System.currentTimeMillis();
        
        client.geocode("Place 1");
        client.geocode("Place 2");
        client.geocode("Place 3");
        
        long end = System.currentTimeMillis();
        long duration = end - start;
        
        // Rate limit: 1 token / second. 3 calls will take ~2 seconds total.
        assertTrue(duration >= 1900, "Should take at least ~2 seconds due to Bucket4j rate limit of 1 req/sec. Took: " + duration + "ms");
        
        mockServer.verify();
    }
}
