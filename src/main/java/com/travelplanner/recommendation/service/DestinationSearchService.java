package com.travelplanner.recommendation.service;

import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.dto.DestinationDto;
import com.travelplanner.recommendation.dto.DestinationSearchFilters;
import com.travelplanner.recommendation.dto.DestinationSearchRequest;
import com.travelplanner.recommendation.dto.DestinationSearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.recommendation.integration.GeminiIntentExtractionService;
import com.travelplanner.recommendation.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DestinationSearchService {

    private final DestinationRepository destinationRepository;
    private final GeminiIntentExtractionService intentService;

    public DestinationSearchResponse search(DestinationSearchRequest request) {
        DestinationSearchFilters filters = intentService.extractFilters(request.getUserQuery(), request.getUserLat(), request.getUserLon());
        log.info("Inferred destination filters: {}", filters);

        List<Destination> candidates = loadCandidates(filters);
        List<DestinationDto> matched = candidates.stream()
                .map(destination -> {
                    Double distanceKm = calculateDistanceKm(request.getUserLat(), request.getUserLon(), destination.getLatitude(), destination.getLongitude());
                    return new Candidate(destination, distanceKm);
                })
                .filter(candidate -> matchesFilters(candidate.destination, candidate.distanceKm, filters))
                .sorted((a, b) -> compareCandidates(a, b, filters, request.getUserLat(), request.getUserLon()))
                .map(candidate -> DestinationDto.from(candidate.destination, candidate.distanceKm))
                .collect(Collectors.toList());

        return DestinationSearchResponse.builder()
                .filters(filters)
                .destinations(matched)
                .build();
    }

    private List<Destination> loadCandidates(DestinationSearchFilters filters) {
        if (filters.getCity() != null && !filters.getCity().isBlank()) {
            List<Destination> cityMatches = destinationRepository.findByCity(filters.getCity());
            if (!cityMatches.isEmpty()) {
                return cityMatches;
            }
        }
        return destinationRepository.findAll();
    }

    private boolean matchesFilters(Destination destination, Double distanceKm, DestinationSearchFilters filters) {
        if (filters.getCategory() != null && !filters.getCategory().isBlank()) {
            try {
                if (!destination.getCategory().name().equalsIgnoreCase(filters.getCategory().trim())) {
                    return false;
                }
            } catch (Exception ignored) {
                return false;
            }
        }

        if (filters.getMaxBudget() != null && destination.getAvgCostPerPerson() != null) {
            if (destination.getAvgCostPerPerson().compareTo(filters.getMaxBudget()) > 0) {
                return false;
            }
        }

        if (filters.getMinRating() != null) {
            if (destination.getAvgRating() < filters.getMinRating()) {
                return false;
            }
        }

        if (filters.getIndoor() != null) {
            if (destination.isIndoor() != filters.getIndoor()) {
                return false;
            }
        }

        if (filters.getTags() != null && !filters.getTags().isEmpty()) {
            List<String> actualTags = destination.getTagList();
            boolean anyMatch = filters.getTags().stream()
                    .map(String::trim)
                    .filter(tag -> !tag.isBlank())
                    .anyMatch(tag -> actualTags.stream().anyMatch(destTag -> destTag.equalsIgnoreCase(tag)));
            if (!anyMatch) {
                return false;
            }
        }

        if (filters.getProximityKm() != null && distanceKm != null) {
            if (distanceKm > filters.getProximityKm()) {
                return false;
            }
        }

        if (filters.getOpenNow() != null && filters.getOpenNow()) {
            if (!isOpenNow(destination)) {
                return false;
            }
        }

        return true;
    }

    private int compareCandidates(Candidate a, Candidate b, DestinationSearchFilters filters, Double userLat, Double userLon) {
        if (filters.getSortBy() != null) {
            String sortBy = filters.getSortBy().trim().toLowerCase(Locale.ROOT);
            if (sortBy.equals("distance") && a.distanceKm != null && b.distanceKm != null) {
                return a.distanceKm.compareTo(b.distanceKm);
            }
            if (sortBy.equals("rating")) {
                return Double.compare(b.destination.getAvgRating(), a.destination.getAvgRating());
            }
            if (sortBy.equals("cost")) {
                return a.destination.getAvgCostPerPerson().compareTo(b.destination.getAvgCostPerPerson());
            }
        }

        if (a.distanceKm != null && b.distanceKm != null) {
            return a.distanceKm.compareTo(b.distanceKm);
        }

        return Double.compare(b.destination.getAvgRating(), a.destination.getAvgRating());
    }

    private boolean isOpenNow(Destination destination) {
        if (destination.getOpeningHours() == null || destination.getOpeningHours().isBlank()) {
            return false;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            var hoursMap = mapper.readValue(destination.getOpeningHours(), java.util.Map.class);
            DayOfWeek today = LocalDateTime.now().getDayOfWeek();
            String key = today.name().substring(0, 3).toLowerCase(Locale.ROOT);
            Object hoursValue = hoursMap.get(key);
            if (hoursValue == null) {
                return false;
            }
            String hours = hoursValue.toString();
            if (hours.isBlank() || !hours.contains("-")) {
                return false;
            }
            String[] parts = hours.split("-");
            if (parts.length != 2) {
                return false;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            var start = java.time.LocalTime.parse(parts[0].trim(), formatter);
            var end = java.time.LocalTime.parse(parts[1].trim(), formatter);
            var now = LocalDateTime.now().toLocalTime();
            return !now.isBefore(start) && !now.isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }

    private Double calculateDistanceKm(Double lat1, Double lon1, double lat2, double lon2) {
        if (lat1 == null || lon1 == null) {
            return null;
        }
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private static class Candidate {
        private final Destination destination;
        private final Double distanceKm;

        Candidate(Destination destination, Double distanceKm) {
            this.destination = destination;
            this.distanceKm = distanceKm;
        }
    }
}
