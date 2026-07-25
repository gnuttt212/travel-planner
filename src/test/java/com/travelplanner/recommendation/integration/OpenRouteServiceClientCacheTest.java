package com.travelplanner.recommendation.integration;

import com.travelplanner.planning.domain.Transportation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

@SpringBootTest(classes = OpenRouteServiceClientCacheTest.TestConfig.class)
@TestPropertySource(properties = {
    "ors.api.key=test-key",
    "ors.api.url=https://api.openrouteservice.org"
})
class OpenRouteServiceClientCacheTest {

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        public OpenRouteServiceClient openRouteServiceClient() {
            return new OpenRouteServiceClient();
        }
        
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("orsRoutes");
        }
    }

    @Autowired
    private OpenRouteServiceClient client;

    @Autowired
    private CacheManager cacheManager;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // Clear cache
        cacheManager.getCache("orsRoutes").clear();
        
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testGetRoute_IsCached() {
        String validJsonResponse = "{\"features\":[{\"properties\":{\"summary\":{\"distance\":5000,\"duration\":600}}}]}";
        
        // We expect only ONE call to the API despite calling the method twice
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openrouteservice.org/v2/directions/")))
                .andRespond(withSuccess(validJsonResponse, org.springframework.http.MediaType.APPLICATION_JSON));

        // First call - should hit the mock server
        OpenRouteServiceClient.RoutingInfo info1 = client.getRoute(10.12345, 106.12345, 10.54321, 106.54321, Transportation.CAR);
        assertEquals(5.0, info1.distanceKm());

        // Second call - should hit the cache and NOT the mock server
        OpenRouteServiceClient.RoutingInfo info2 = client.getRoute(10.12345, 106.12345, 10.54321, 106.54321, Transportation.CAR);
        assertEquals(5.0, info2.distanceKm());

        mockServer.verify();
        
        // Verify cache content
        String cacheKey = String.format(java.util.Locale.US, "%.5f,%.5f-%.5f,%.5f-%s", 10.12345, 106.12345, 10.54321, 106.54321, Transportation.CAR.name());
        assertNotNull(cacheManager.getCache("orsRoutes").get(cacheKey));
    }
}
