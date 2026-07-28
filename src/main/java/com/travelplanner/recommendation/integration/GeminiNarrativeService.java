package com.travelplanner.recommendation.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.recommendation.domain.Destination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeminiNarrativeService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private GeminiNarrativeService self;

    public GeminiNarrativeService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String generateNarrative(String variantName, List<Destination> destinations) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("${")) {
            return "";
        }
        if (destinations == null || destinations.isEmpty()) {
            return "";
        }

        // Generate cache key from destination IDs
        String destIds = destinations.stream()
                .map(d -> d.getId().toString())
                .collect(Collectors.joining(","));
        String cacheKey = variantName + "-" + destIds.hashCode();

        try {
            return self.fetchNarrativeCached(cacheKey, variantName, destinations);
        } catch (Exception e) {
            log.error("Gemini API failed, returning empty narrative: {}", e.getMessage());
            return "";
        }
    }

    @org.springframework.cache.annotation.Cacheable(value = "aiNarrativeCache", key = "#cacheKey")
    public String fetchNarrativeCached(String cacheKey, String variantName, List<Destination> destinations) {
        String destNames = destinations.stream()
                .map(Destination::getName)
                .collect(Collectors.joining(", "));

        String promptText = String.format(
                "Hãy viết 3 câu giới thiệu thật hấp dẫn về lịch trình du lịch mang phong cách '%s', đi qua các địa điểm: %s. Dùng văn phong năng động, xưng hô 'bạn'.",
                variantName, destNames
        );

        String url = apiUrl + "?key=" + apiKey;

        try {
            // Build Gemini request body
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
                    return parts.get(0).path("text").asText().trim();
                }
            }
            throw new RuntimeException("Unexpected response format from Gemini");
        } catch (Exception e) {
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }
}
