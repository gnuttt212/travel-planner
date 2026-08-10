package com.travelplanner.recommendation.dto;

import com.travelplanner.recommendation.domain.Destination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationDto {
    private String id;
    private String name;
    private String description;
    private String city;
    private String address;
    private String category;
    private double latitude;
    private double longitude;
    private double avgRating;
    private int reviewCount;
    private BigDecimal avgCostPerPerson;
    private List<String> tags;
    private boolean indoor;
    private String imageUrl;
    private Double distanceKm;

    public static DestinationDto from(Destination destination, Double distanceKm) {
        return DestinationDto.builder()
                .id(Optional.ofNullable(destination.getId()).map(Object::toString).orElse(null))
                .name(destination.getName())
                .description(destination.getDescription())
                .city(destination.getCity())
                .address(destination.getAddress())
                .category(Optional.ofNullable(destination.getCategory()).map(Enum::name).orElse(null))
                .latitude(destination.getLatitude())
                .longitude(destination.getLongitude())
                .avgRating(destination.getAvgRating())
                .reviewCount(destination.getReviewCount())
                .avgCostPerPerson(destination.getAvgCostPerPerson())
                .tags(destination.getTagList())
                .indoor(destination.isIndoor())
                .imageUrl(destination.getImageUrl())
                .distanceKm(distanceKm)
                .build();
    }
}
