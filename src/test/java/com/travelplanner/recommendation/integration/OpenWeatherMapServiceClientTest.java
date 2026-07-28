package com.travelplanner.recommendation.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenWeatherMapServiceClientTest {

    private OpenWeatherMapServiceClient client;
    private MockRestServiceServer mockServer;
    private LocalDate today;
    private long todayEpoch;

    @BeforeEach
    void setUp() {
        client = new OpenWeatherMapServiceClient();
        ReflectionTestUtils.setField(client, "apiKey", "test-key");
        ReflectionTestUtils.setField(client, "apiUrl", "https://api.openweathermap.org/data/2.5/forecast");
        ReflectionTestUtils.setField(client, "self", client);
        
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
        
        today = LocalDate.now();
        todayEpoch = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    }

    @Test
    void testIsRaining_Yes() {
        String jsonResponse = "{\"list\":[{\"dt\":" + todayEpoch + ",\"weather\":[{\"id\":500,\"main\":\"Rain\"}]}]}";
        
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openweathermap.org/data/2.5/forecast")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        boolean raining = client.isRaining(10.0, 106.0, today);
        assertTrue(raining);
        mockServer.verify();
    }

    @Test
    void testIsRaining_No() {
        String jsonResponse = "{\"list\":[{\"dt\":" + todayEpoch + ",\"weather\":[{\"id\":800,\"main\":\"Clear\"}]}]}";
        
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openweathermap.org/data/2.5/forecast")))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        boolean raining = client.isRaining(10.0, 106.0, today);
        assertFalse(raining);
        mockServer.verify();
    }

    @Test
    void testIsRaining_FallbackOn401() {
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openweathermap.org/data/2.5/forecast")))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        boolean raining = client.isRaining(10.0, 106.0, today);
        assertFalse(raining); // Fallback is false
        mockServer.verify();
    }
    
    @Test
    void testIsRaining_FallbackOnMalformedJson() {
        // Missing list array
        String jsonResponse = "{\"cod\":\"200\",\"message\":0,\"cnt\":40}";
        
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://api.openweathermap.org/data/2.5/forecast")))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        boolean raining = client.isRaining(10.0, 106.0, today);
        assertFalse(raining); // Fallback is false
        mockServer.verify();
    }
}
