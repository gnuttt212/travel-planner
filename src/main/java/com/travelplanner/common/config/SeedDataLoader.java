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
                .name((String) item.get("name"))
                .description((String) item.get("description"))
                .city((String) item.get("city"))
                .address((String) item.get("address"))
                .category(DestinationCategory.valueOf((String) item.get("category")))
                .latitude(((Number) item.get("latitude")).doubleValue())
                .longitude(((Number) item.get("longitude")).doubleValue())
                .avgRating(((Number) item.get("avgRating")).doubleValue())
                .reviewCount(((Number) item.get("reviewCount")).intValue())
                .avgCostPerPerson(new BigDecimal(item.get("avgCostPerPerson").toString()))
                .tags((String) item.get("tags"))
                .openingHours((String) item.get("openingHours"))
                .indoor((Boolean) item.get("indoor"))
                .imageUrl((String) item.get("imageUrl"))
                .bestMonths((String) item.get("bestMonths"))
                .build();
            destinations.add(dest);
        }
        
        destinationRepository.saveAll(destinations);
        log.info("Seeded {} destinations.", destinations.size());
    }
}
