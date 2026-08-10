package com.travelplanner.recommendation.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@Slf4j
public class NominatimGeocodingClient {

    @Value("${nominatim.api.url:https://nominatim.openstreetmap.org/search}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private NominatimGeocodingClient self;

    private static final Bucket rateLimiter = Bucket.builder()
            .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofSeconds(1))))
            .build();

    public NominatimGeocodingClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public record GeocodingResult(double lat, double lon, String displayName) {}

    public GeocodingResult geocode(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String cacheKey = address.trim().toLowerCase();
        try {
            return self.fetchGeocodeCached(cacheKey, address);
        } catch (Exception e) {
            log.warn("Failed to geocode address: {}", address, e);
            return null;
        }
    }

    @Cacheable(value = "geocodingCache", key = "#cacheKey", unless = "#result == null")
    public GeocodingResult fetchGeocodeCached(String cacheKey, String address) {
        try {
            rateLimiter.asBlocking().consume(1);
            String url = apiUrl + "?q=" + URLEncoder.encode(address, StandardCharsets.UTF_8) + "&format=json&limit=1";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelPlanner/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                double lat = first.path("lat").asDouble();
                double lon = first.path("lon").asDouble();
                String displayName = first.path("display_name").asText();
                return new GeocodingResult(lat, lon, displayName);
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Rate limiter interrupted", e);
            throw new RuntimeException("Rate limiter interrupted", e);
        } catch (Exception e) {
            log.error("Error calling Nominatim API", e);
            throw new RuntimeException("Error calling Nominatim API", e);
        }
    }
}
