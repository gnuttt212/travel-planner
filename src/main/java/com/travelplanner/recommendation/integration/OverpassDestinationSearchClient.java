package com.travelplanner.recommendation.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.domain.DestinationCategory;
import com.travelplanner.recommendation.port.DestinationSearchPort;
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

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Overpass API (OpenStreetMap) client for searching POIs near a coordinate.
 * <p>
 * - Rate-limited to 1 request per 5 seconds via Bucket4j (Overpass fair-use policy).
 * - Results are cached in Redis ("overpassSearch") for 7 days.
 * - On any error (timeout, parse failure, API down), returns empty list — never throws.
 * - User-Agent header is mandatory per OSM usage policy.
 */
@Service
@Slf4j
public class OverpassDestinationSearchClient implements DestinationSearchPort {

    @Value("${overpass.api.url:https://overpass-api.de/api/interpreter}")
    private String apiUrl;

    @Value("${destination.dynamic-search.max-results:30}")
    private int maxResults;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private OverpassDestinationSearchClient self;

    /** Rate limiter: 1 request per 5 seconds to respect Overpass fair-use policy */
    private static final Bucket rateLimiter = Bucket.builder()
            .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofSeconds(5))))
            .build();

    /**
     * Mapping from OSM tags to DestinationCategory.
     * Key format: "tagKey=tagValue" → DestinationCategory.
     */
    private static final Map<String, DestinationCategory> OSM_TAG_MAPPING = Map.ofEntries(
            Map.entry("tourism=museum", DestinationCategory.MUSEUM),
            Map.entry("tourism=viewpoint", DestinationCategory.VIEWPOINT),
            Map.entry("tourism=attraction", DestinationCategory.ENTERTAINMENT),
            Map.entry("amenity=cafe", DestinationCategory.CAFE),
            Map.entry("amenity=restaurant", DestinationCategory.RESTAURANT),
            Map.entry("amenity=bar", DestinationCategory.BAR),
            Map.entry("amenity=fast_food", DestinationCategory.STREET_FOOD),
            Map.entry("amenity=food_court", DestinationCategory.STREET_FOOD),
            Map.entry("amenity=place_of_worship", DestinationCategory.TEMPLE),
            Map.entry("leisure=park", DestinationCategory.PARK),
            Map.entry("leisure=garden", DestinationCategory.GARDEN),
            Map.entry("leisure=beach_resort", DestinationCategory.BEACH),
            Map.entry("natural=beach", DestinationCategory.BEACH),
            Map.entry("shop=mall", DestinationCategory.SHOPPING),
            Map.entry("shop=marketplace", DestinationCategory.MARKET)
    );

    public OverpassDestinationSearchClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<Destination> searchNearby(double lat, double lon, double radiusKm, String city) {
        try {
            // Cache key combines city + radius for consistent lookups
            String cacheKey = (city != null ? city.trim().toLowerCase() : "unknown")
                    + "_" + (int) radiusKm;
            return self.fetchSearchCached(cacheKey, lat, lon, radiusKm, city);
        } catch (Exception e) {
            // Graceful fallback: never let Overpass errors propagate to caller
            log.warn("Overpass search failed for city={}, falling back to empty list: {}", city, e.getMessage());
            return List.of();
        }
    }

    /**
     * Cached fetch from Overpass API. Cache key is city_radiusKm.
     * Throws on error so @Cacheable does NOT cache failures.
     */
    @Cacheable(value = "overpassSearch", key = "#cacheKey")
    public List<Destination> fetchSearchCached(String cacheKey, double lat, double lon,
                                                double radiusKm, String city) {
        try {
            // Block thread until rate limit token is available
            rateLimiter.asBlocking().consume(1);

            int radiusMeters = (int) (radiusKm * 1000);
            String query = buildOverpassQuery(lat, lon, radiusMeters);

            // Use POST with form-encoded body (recommended by Overpass API for reliability)
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelPlanner/1.0 (travel-planner-app)");
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            String body = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            log.info("Calling Overpass API for city={}, radius={}km, lat={}, lon={}", city, radiusKm, lat, lon);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            return parseOverpassResponse(response.getBody(), city);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Rate limiter interrupted during Overpass call", e);
            throw new RuntimeException("Rate limiter interrupted", e);
        } catch (Exception e) {
            log.error("Error calling Overpass API for city={}: {}", city, e.getMessage());
            throw new RuntimeException("Error calling Overpass API", e);
        }
    }

    /**
     * Build an Overpass QL query that searches for common POI types
     * within a radius around the given coordinate.
     *
     * The query covers: tourism, amenity (food/drink/worship), leisure, shops, beaches.
     * Results are limited to maxResults via "out body {limit}".
     */
    private String buildOverpassQuery(double lat, double lon, int radiusMeters) {
        return String.format(Locale.US,
                "[out:json][timeout:25];" +
                "(" +
                "  node[\"tourism\"~\"museum|viewpoint|attraction\"](around:%d,%f,%f);" +
                "  node[\"amenity\"~\"cafe|restaurant|bar|fast_food|food_court|place_of_worship\"](around:%d,%f,%f);" +
                "  node[\"leisure\"~\"park|garden|beach_resort\"](around:%d,%f,%f);" +
                "  node[\"shop\"~\"mall|marketplace\"](around:%d,%f,%f);" +
                "  node[\"natural\"=\"beach\"](around:%d,%f,%f);" +
                ");" +
                "out body %d;",
                radiusMeters, lat, lon,
                radiusMeters, lat, lon,
                radiusMeters, lat, lon,
                radiusMeters, lat, lon,
                radiusMeters, lat, lon,
                maxResults
        );
    }

    /**
     * Parse Overpass JSON response into Destination entities.
     *
     * Response structure:
     * {
     *   "elements": [
     *     {
     *       "type": "node",
     *       "id": 123456,
     *       "lat": 10.762,
     *       "lon": 106.660,
     *       "tags": {
     *         "name": "Bến Thành Market",
     *         "amenity": "marketplace",
     *         "opening_hours": "Mo-Su 06:00-18:00",
     *         ...
     *       }
     *     },
     *     ...
     *   ]
     * }
     *
     * For each element:
     * 1. Extract lat, lon from top-level fields
     * 2. Extract name from tags.name (skip if missing — unnamed POI)
     * 3. Determine DestinationCategory by matching tags against OSM_TAG_MAPPING
     * 4. Build a Destination entity with sensible defaults for missing fields
     */
    private List<Destination> parseOverpassResponse(String responseBody, String city) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode elements = root.path("elements");

        if (!elements.isArray()) {
            log.warn("Overpass response has no 'elements' array");
            return List.of();
        }

        List<Destination> results = new ArrayList<>();

        for (JsonNode element : elements) {
            // Skip elements without a name — unnamed POIs are not useful for trip planning
            JsonNode tags = element.path("tags");
            String name = tags.path("name").asText(null);
            if (name == null || name.isBlank()) {
                continue;
            }

            double lat = element.path("lat").asDouble();
            double lon = element.path("lon").asDouble();

            // Determine category by matching OSM tags against our mapping table.
            // Try each known tag key (tourism, amenity, leisure, shop, natural)
            // and look up "key=value" in OSM_TAG_MAPPING.
            DestinationCategory category = resolveCategory(tags);
            if (category == null) {
                // If no matching category found, skip this POI
                continue;
            }

            String description = tags.path("description").asText(
                    tags.path("name:en").asText(name));
            String address = tags.path("addr:full").asText(
                    tags.path("addr:street").asText(""));

            // Build Destination entity with sensible defaults for fields
            // that OSM doesn't provide (rating, cost, etc.)
            Destination dest = Destination.builder()
                    .name(name)
                    .description(description)
                    .city(city)
                    .address(address)
                    .category(category)
                    .latitude(lat)
                    .longitude(lon)
                    .avgRating(4.0)                     // Default rating for unrated POIs
                    .reviewCount(0)
                    .avgCostPerPerson(BigDecimal.ZERO)   // Free by default; user can edit
                    .indoor(isIndoor(category, tags))
                    .imageUrl(null)
                    .build();

            results.add(dest);
        }

        log.info("Parsed {} destinations from Overpass response for city={}", results.size(), city);
        return results;
    }

    /**
     * Resolve DestinationCategory from OSM tags by checking known tag keys
     * (tourism, amenity, leisure, shop, natural) against OSM_TAG_MAPPING.
     *
     * @return matched category, or null if no mapping found
     */
    private DestinationCategory resolveCategory(JsonNode tags) {
        String[] tagKeys = {"tourism", "amenity", "leisure", "shop", "natural"};
        for (String key : tagKeys) {
            String value = tags.path(key).asText(null);
            if (value != null) {
                DestinationCategory cat = OSM_TAG_MAPPING.get(key + "=" + value);
                if (cat != null) {
                    return cat;
                }
            }
        }
        return null;
    }

    /**
     * Heuristic: determine if a POI is indoor based on its category and OSM tags.
     */
    private boolean isIndoor(DestinationCategory category, JsonNode tags) {
        // Categories that are typically indoor
        if (category == DestinationCategory.MUSEUM ||
            category == DestinationCategory.CAFE ||
            category == DestinationCategory.RESTAURANT ||
            category == DestinationCategory.BAR ||
            category == DestinationCategory.SHOPPING) {
            return true;
        }
        // Check explicit indoor tag from OSM
        String indoor = tags.path("indoor").asText(null);
        return "yes".equalsIgnoreCase(indoor);
    }
}
