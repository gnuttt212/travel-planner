package com.travelplanner.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaypointDto {
    private String destinationId;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer originalOrder;
}
