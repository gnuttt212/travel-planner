package com.travelplanner.recommendation.integration;

import com.travelplanner.recommendation.domain.Destination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiNarrativeServiceTest {

    private GeminiNarrativeService service;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        service = new GeminiNarrativeService();
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        ReflectionTestUtils.setField(service, "apiUrl", "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent");
        ReflectionTestUtils.setField(service, "self", service);
        
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testGenerateNarrative_Success() {
        String jsonResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Chuyến đi tuyệt vời!\"}]}}]}";
        
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://generativelanguage.googleapis.com")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Destination d = new Destination();
        d.setId(UUID.randomUUID());
        d.setName("Ben Thanh");
        
        String narrative = service.generateNarrative("Cân bằng", List.of(d));
        assertEquals("Chuyến đi tuyệt vời!", narrative);
        mockServer.verify();
    }

    @Test
    void testGenerateNarrative_FallbackOnError() {
        mockServer.expect(ExpectedCount.once(), 
                requestTo(org.hamcrest.Matchers.startsWith("https://generativelanguage.googleapis.com")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        Destination d = new Destination();
        d.setId(UUID.randomUUID());
        d.setName("Ben Thanh");
        
        String narrative = service.generateNarrative("Cân bằng", List.of(d));
        assertEquals("", narrative); // Fallback to empty string
        mockServer.verify();
    }
}
