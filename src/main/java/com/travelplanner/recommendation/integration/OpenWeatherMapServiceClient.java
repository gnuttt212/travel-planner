package com.travelplanner.recommendation.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Slf4j
public class OpenWeatherMapServiceClient {

    @Value("${openweathermap.api.key:}")
    private String apiKey;

    @Value("${openweathermap.api.url:https://api.openweathermap.org/data/2.5/forecast}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private OpenWeatherMapServiceClient self;

    public OpenWeatherMapServiceClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public boolean isRaining(double lat, double lon, LocalDate targetDate) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("${")) {
            log.warn("OpenWeatherMap API Key is missing. Assuming no rain.");
            return false;
        }

        // Round coordinates to 4 decimal places (~11 meters) to improve cache hit rate
        String cacheKey = String.format(java.util.Locale.US, "%.4f,%.4f-%s", lat, lon, targetDate.toString());
        try {
            return self.fetchWeatherCached(cacheKey, lat, lon, targetDate);
        } catch (Exception e) {
            log.error("Weather check failed, falling back to false (no rain). Error: {}", e.getMessage());
            return false;
        }
    }

    @org.springframework.cache.annotation.Cacheable(value = "weatherCache", key = "#cacheKey")
    public boolean fetchWeatherCached(String cacheKey, double lat, double lon, LocalDate targetDate) {
        String url = String.format(java.util.Locale.US, "%s?lat=%.4f&lon=%.4f&appid=%s", apiUrl, lat, lon, apiKey);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode list = root.path("list");
            
            if (list.isMissingNode() || !list.isArray()) {
                throw new RuntimeException("Malformed response: missing 'list' array");
            }

            for (JsonNode item : list) {
                long dt = item.path("dt").asLong();
                LocalDate forecastDate = Instant.ofEpochSecond(dt).atZone(ZoneId.systemDefault()).toLocalDate();
                
                if (forecastDate.equals(targetDate)) {
                    JsonNode weatherArray = item.path("weather");
                    if (weatherArray.isArray() && weatherArray.size() > 0) {
                        int weatherId = weatherArray.get(0).path("id").asInt();
                        // Weather IDs: 2xx (Thunderstorm), 3xx (Drizzle), 5xx (Rain), 6xx (Snow)
                        if (weatherId >= 200 && weatherId < 700) {
                            return true; // Found rain/bad weather on this date
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to fetch weather from OpenWeatherMap: {}", e.getMessage());
            // Fallback: assume no rain if API fails (429, 401, timeout, malformed)
            // Throwing exception here would cause the @Cacheable to not cache, which is what we want for errors!
            // But if we throw exception, the caller will crash unless caught.
            // Wait, we want to return false so the caller doesn't crash, but we DON'T want to cache `false` if it was an API error!
            // Actually, if we throw an exception here, we can catch it in `isRaining`!
            throw new RuntimeException("OWM API Error: " + e.getMessage(), e);
        }
    }
}
