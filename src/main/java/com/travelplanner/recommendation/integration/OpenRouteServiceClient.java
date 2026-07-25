package com.travelplanner.recommendation.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planning.domain.Transportation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenRouteServiceClient {

    private static final Logger log = LoggerFactory.getLogger(OpenRouteServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ors.api.key:}")
    private String apiKey;

    @Value("${ors.api.url:https://api.openrouteservice.org}")
    private String apiUrl;

    public OpenRouteServiceClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public RoutingInfo getRoute(double startLat, double startLon, double endLat, double endLon, Transportation transport) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("ORS API Key is missing");
        }

        String profile = switch (transport) {
            case MOTORBIKE -> "driving-car"; // ORS free tier doesn't always have motorbike, driving-car is a safe fallback
            case CAR -> "driving-car";
            case PUBLIC_TRANSPORT -> "driving-car"; // ORS doesn't do full transit natively in free tier, using car as baseline
        };

        // Note: ORS uses lon,lat order for coordinates
        String url = String.format("%s/v2/directions/%s?api_key=%s&start=%s,%s&end=%s,%s",
                apiUrl, profile, apiKey, startLon, startLat, endLon, endLat);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode features = root.path("features");
                if (features.isArray() && features.size() > 0) {
                    JsonNode summary = features.get(0).path("properties").path("summary");
                    double distanceKm = summary.path("distance").asDouble() / 1000.0; // API returns meters
                    int durationMinutes = (int) Math.ceil(summary.path("duration").asDouble() / 60.0); // API returns seconds
                    return new RoutingInfo(distanceKm, durationMinutes);
                }
            }
            throw new RuntimeException("Invalid response from ORS");
        } catch (Exception e) {
            log.warn("Failed to fetch route from ORS: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch route from ORS", e);
        }
    }

    public record RoutingInfo(double distanceKm, int durationMinutes) {}
}
