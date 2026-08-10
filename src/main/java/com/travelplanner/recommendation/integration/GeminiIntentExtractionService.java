package com.travelplanner.recommendation.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.recommendation.dto.DestinationSearchFilters;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiIntentExtractionService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DestinationSearchFilters extractFilters(String userQuery, Double userLat, Double userLon) {
        if (userQuery == null || userQuery.isBlank()) {
            return DestinationSearchFilters.builder().build();
        }

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("${")) {
            log.warn("Gemini API key is not configured. Returning empty filters.");
            return DestinationSearchFilters.builder().build();
        }

        String promptText = buildPrompt(userQuery, userLat, userLon);

        String url = apiUrl + "?key=" + apiKey;
        try {
            Map<String, Object> part = new HashMap<>();
            part.put("text", promptText);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));

            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            String response = restTemplate.postForObject(url, request, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    String json = extractJson(text);
                    if (json != null) {
                        return objectMapper.readValue(json, DestinationSearchFilters.class);
                    }
                }
            }

            log.warn("Gemini response did not return expected JSON object for query: {}", userQuery);
        } catch (Exception e) {
            log.error("Error extracting search filters from Gemini: {}", e.getMessage(), e);
        }

        return DestinationSearchFilters.builder().build();
    }

    private String extractJson(String text) {
        if (text == null) {
            return null;
        }

        int start = text.indexOf('{');
        if (start < 0) {
            return null;
        }

        int depth = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private String buildPrompt(String userQuery, Double userLat, Double userLon) {
        String location = "unknown";
        if (userLat != null && userLon != null) {
            location = String.format("latitude=%s, longitude=%s", userLat, userLon);
        }

        return "You are a search intent extractor for a travel destination search system. " +
                "The user will provide a natural language travel query. Respond with ONLY a valid JSON object and no additional text. " +
                "The JSON object must follow this schema:\n" +
                "{\n" +
                "  \"category\": string|null,\n" +
                "  \"tags\": [string]|null,\n" +
                "  \"city\": string|null,\n" +
                "  \"maxBudget\": number|null,\n" +
                "  \"minRating\": number|null,\n" +
                "  \"proximityKm\": number|null,\n" +
                "  \"indoor\": boolean|null,\n" +
                "  \"openNow\": boolean|null,\n" +
                "  \"sortBy\": string|null\n" +
                "}\n" +
                "Rules:\n" +
                "- Do not include markdown, explanation, or any text outside the JSON object.\n" +
                "- If a field is not specified, use null or omit it.\n" +
                "- City names should be preserved as they appear in the query.\n" +
                "- Budget should be the maximum amount per person in local currency, without currency symbols if possible.\n" +
                "- Tags should be simple keywords like coffee, beach, wifi, breakfast, romantic.\n" +
                "- sortBy may be one of: rating, distance, cost.\n" +
                "- proximityKm should be a number expressing the maximum desired distance from the user location.\n" +
                "User location: " + location + "\n" +
                "User query: " + userQuery;
    }
}
