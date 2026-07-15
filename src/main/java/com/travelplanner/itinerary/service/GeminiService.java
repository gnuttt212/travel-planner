package com.travelplanner.itinerary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.itinerary.dto.DestinationResponse;
import com.travelplanner.user.domain.UserPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper;

    public String generateDetailedItinerary(List<DestinationResponse> topDestinations, UserPreference userPreference) {
        RestTemplate restTemplate = new RestTemplate();

        String prompt = buildPrompt(topDestinations, userPreference);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the body according to Gemini API specs
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String url = apiUrl + "?key=" + apiKey;
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            return "Unable to generate itinerary at this time. Please try again later.";
        }
    }

    private String buildPrompt(List<DestinationResponse> topDestinations, UserPreference userPreference) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert travel planner AI.\n");
        prompt.append("Based on the following top ranked destinations that perfectly match the user's budget and month of travel, ");
        prompt.append("generate a highly detailed 3-day itinerary.\n\n");
        
        prompt.append("User Preferences:\n");
        if (userPreference != null) {
            prompt.append("- Travel Style: ").append(userPreference.getTravelStyle()).append("\n");
            prompt.append("- Group Type: ").append(userPreference.getGroupType()).append("\n");
            prompt.append("- Preferred Tags: ").append(userPreference.getTagWeights() != null ? userPreference.getTagWeights().keySet() : "Any").append("\n");
        }
        
        prompt.append("\nTop Destinations (Use ONLY these for the itinerary):\n");
        for (DestinationResponse dest : topDestinations) {
            prompt.append("- ").append(dest.name()).append(" (").append(dest.country()).append(")\n");
            prompt.append("  Description: ").append(dest.description()).append("\n");
        }
        
        prompt.append("\nPlease format the itinerary clearly with Day 1, Day 2, Day 3 headings and detailed activities.");
        return prompt.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
        }
        return "No response generated.";
    }
}
