package com.travelplanner.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.itinerary.domain.Destination;
import com.travelplanner.itinerary.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DestinationRepository destinationRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (destinationRepository.count() == 0) {
            log.info("No destinations found. Seeding mock data...");
            try (InputStream is = getClass().getResourceAsStream("/mock_destinations.json")) {
                List<Destination> destinations = objectMapper.readValue(is, new TypeReference<List<Destination>>() {});
                destinationRepository.saveAll(destinations);
                log.info("Seeded {} destinations.", destinations.size());
            } catch (Exception e) {
                log.error("Failed to seed destinations data", e);
            }
        } else {
            log.info("Destinations table is not empty. Skipping seed.");
        }
    }
}
