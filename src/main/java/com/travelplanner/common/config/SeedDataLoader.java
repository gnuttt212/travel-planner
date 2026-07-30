package com.travelplanner.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.domain.DestinationCategory;
import com.travelplanner.recommendation.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDataLoader implements CommandLineRunner {
    
    private final DestinationRepository destinationRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public void run(String... args) throws Exception {
        if (destinationRepository.count() > 0) {
            log.info("Destinations already seeded. Skipping.");
            return;
        }
        
        log.info("Seeding destinations from JSON...");
        InputStream is = new ClassPathResource("seed_destinations.json").getInputStream();
        List<Map<String, Object>> data = objectMapper.readValue(is, new TypeReference<>() {});
        
        List<Destination> destinations = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Destination dest = Destination.builder()
                .name(asString(item.get("name")))
                .description(asString(item.get("description")))
                .city(asString(item.get("city")))
                .address(asString(item.get("address")))
                .category(DestinationCategory.valueOf(asString(item.get("category"))))
                .latitude(asDouble(item.get("latitude")))
                .longitude(asDouble(item.get("longitude")))
                .avgRating(asDouble(item.get("avgRating")))
                .reviewCount(asInt(item.get("reviewCount")))
                .avgCostPerPerson(new BigDecimal(item.get("avgCostPerPerson").toString()))
                .tags(asJsonText(item.get("tags")))
                .openingHours(asJsonText(item.get("openingHours")))
                .indoor(asBoolean(item.get("indoor")))
                .imageUrl(asString(item.get("imageUrl")))
                .bestMonths(asJsonText(item.get("bestMonths")))
                .build();
            destinations.add(dest);
        }

        destinationRepository.saveAll(destinations);
        log.info("Seeded {} destinations.", destinations.size());
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String asJsonText(Object value) throws Exception {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return objectMapper.writeValueAsString(value);
    }

    private double asDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        return ((Number) value).doubleValue();
    }

    private int asInt(Object value) {
        if (value == null) {
            return 0;
        }
        return ((Number) value).intValue();
    }

    private boolean asBoolean(Object value) {
        if (value == null) {
            return false;
        }
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(value.toString());
    }
}
