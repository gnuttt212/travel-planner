package com.travelplanner.itinerary.mapper;

import com.travelplanner.itinerary.domain.Destination;
import com.travelplanner.itinerary.dto.DestinationRequest;
import com.travelplanner.itinerary.dto.DestinationResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper thu cong (khong dung MapStruct) de giu du an don gian o giai
 * doan khoi dau. Neu so luong DTO tang len nhieu, co the thay bang
 * MapStruct de giam boilerplate.
 */
@Component
public class DestinationMapper {

    public Destination toEntity(DestinationRequest request) {
        return Destination.builder()
                .name(request.name())
                .country(request.country())
                .description(request.description())
                .tags(request.tags())
                .bestMonths(request.bestMonths())
                .avgCostPerDay(request.avgCostPerDay())
                .build();
    }

    public DestinationResponse toResponse(Destination entity) {
        return new DestinationResponse(
                entity.getId(),
                entity.getName(),
                entity.getCountry(),
                entity.getDescription(),
                entity.getTags(),
                entity.getBestMonths(),
                entity.getAvgCostPerDay(),
                entity.getPopularityScore(),
                entity.getLatitude(),
                entity.getLongitude(),
                null // recommendationReason will be populated by the service if needed
        );
    }
}
