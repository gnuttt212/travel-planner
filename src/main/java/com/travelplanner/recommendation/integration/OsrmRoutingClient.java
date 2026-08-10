package com.travelplanner.recommendation.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

@Service
@Slf4j
public class OsrmRoutingClient {

    @Value("${osrm.api.url:https://router.project-osrm.org}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private OsrmRoutingClient self;

    public OsrmRoutingClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public record RoutingResult(double distanceKm, int durationMinutes) {}

    public RoutingResult route(double startLat, double startLon, double endLat, double endLon) {
        String cacheKey = String.format(Locale.US, "%.5f,%.5f-%.5f,%.5f", startLat, startLon, endLat, endLon);
        try {
            return self.fetchRouteCached(cacheKey, startLat, startLon, endLat, endLon);
        } catch (Exception e) {
            log.warn("Failed to fetch route from OSRM, using fallback", e);
            return haversineFallback(startLat, startLon, endLat, endLon);
        }
    }

    @Cacheable(value = "osrmRoutes", key = "#cacheKey")
    public RoutingResult fetchRouteCached(String cacheKey, double startLat, double startLon, double endLat, double endLon) {
        try {
            String url = String.format(Locale.US, "%s/route/v1/driving/%.5f,%.5f;%.5f,%.5f?overview=false", 
                    apiUrl, startLon, startLat, endLon, endLat);
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            if ("Ok".equals(root.path("code").asText()) && root.has("routes") && root.path("routes").size() > 0) {
                JsonNode route = root.path("routes").get(0);
                double distanceMeters = route.path("distance").asDouble();
                double durationSeconds = route.path("duration").asDouble();
                
                double distanceKm = distanceMeters / 1000.0;
                int durationMinutes = (int) Math.ceil(durationSeconds / 60.0);
                
                return new RoutingResult(distanceKm, durationMinutes);
            } else {
                throw new RuntimeException("Invalid response or code not Ok: " + root.path("code").asText());
            }
        } catch (Exception e) {
            log.error("Error fetching route from OSRM", e);
            throw new RuntimeException("Error fetching route from OSRM", e);
        }
    }

    private RoutingResult haversineFallback(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = R * c;
        
        int durationMinutes = Math.max(5, (int) Math.ceil((distanceKm / 30.0) * 60));
        return new RoutingResult(distanceKm, durationMinutes);
    }
}
