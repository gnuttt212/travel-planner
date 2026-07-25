package com.travelplanner.recommendation.integration;

import com.travelplanner.planning.domain.Transportation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class OpenRouteServiceClientTest {

    private OpenRouteServiceClient client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        client = new OpenRouteServiceClient();
        ReflectionTestUtils.setField(client, "apiKey", "test-key");
        ReflectionTestUtils.setField(client, "apiUrl", "https://api.openrouteservice.org");
        // We set 'self' to 'client' for testing without Spring Context (cache won't trigger in this plain unit test)
        ReflectionTestUtils.setField(client, "self", client);
        
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testGetRoute_Success() {
        String validJsonResponse = "{\"features\":[{\"properties\":{\"summary\":{\"distance\":5000,\"duration\":600}}}]}";
        
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openrouteservice.org/v2/directions/")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(validJsonResponse, MediaType.APPLICATION_JSON));

        OpenRouteServiceClient.RoutingInfo info = client.getRoute(10.0, 106.0, 10.1, 106.1, Transportation.CAR);
        
        assertEquals(5.0, info.distanceKm()); // 5000 meters = 5 km
        assertEquals(10, info.durationMinutes()); // 600 seconds = 10 minutes
        mockServer.verify();
    }

    @Test
    void testGetRoute_MalformedJson_ThrowsException() {
        // Missing "summary" field
        String malformedJson = "{\"features\":[{\"properties\":{}}]}";
        
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openrouteservice.org/v2/directions/")))
                .andRespond(withSuccess(malformedJson, MediaType.APPLICATION_JSON));

        assertThrows(RuntimeException.class, () -> 
            client.getRoute(10.0, 106.0, 10.1, 106.1, Transportation.CAR)
        );
        mockServer.verify();
    }

    @Test
    void testGetRoute_Http429_RateLimit() {
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openrouteservice.org/v2/directions/")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThrows(RuntimeException.class, () -> 
            client.getRoute(10.0, 106.0, 10.1, 106.1, Transportation.CAR)
        );
        mockServer.verify();
    }

    @Test
    void testGetRoute_Http401_Unauthorized() {
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openrouteservice.org/v2/directions/")))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(RuntimeException.class, () -> 
            client.getRoute(10.0, 106.0, 10.1, 106.1, Transportation.CAR)
        );
        mockServer.verify();
    }
    
    @Test
    void testGetRoute_Timeout() {
        // Mock a timeout or internal server error
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openrouteservice.org/v2/directions/")))
                .andRespond(withServerError());

        assertThrows(RuntimeException.class, () -> 
            client.getRoute(10.0, 106.0, 10.1, 106.1, Transportation.CAR)
        );
        mockServer.verify();
    }
}
