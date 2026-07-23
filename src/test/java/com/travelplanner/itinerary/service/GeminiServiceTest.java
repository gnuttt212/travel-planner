package com.travelplanner.itinerary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.itinerary.dto.DestinationResponse;
import com.travelplanner.user.domain.UserPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(geminiService, "apiKey", "test-key");
        org.springframework.test.util.ReflectionTestUtils.setField(geminiService, "apiUrl", "https://example.test");
    }

    @Test
    void shouldReturnFallbackWhenExternalApiFails() {
        lenient().when(restTemplate.postForObject(any(String.class), any(), eq(Map.class))).thenThrow(new RestClientException("boom"));

        String result = geminiService.generateDetailedItinerary(
        List.of(new DestinationResponse("id", "Paris", "France", "desc", null, null, null, null, null, null, null)),
        new UserPreference(),
        "Clear"
    );

        assertTrue(result.contains("Unable to generate itinerary"));
    }
}
