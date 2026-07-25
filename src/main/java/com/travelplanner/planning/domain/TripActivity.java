package com.travelplanner.planning.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "trip_activities")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TripActivity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "trip_id")
    private UUID tripId;
    
    private UUID destinationId;
    private int orderIndex;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private int estimatedDurationMinutes;
    private BigDecimal estimatedCost;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ActivityStatus status = ActivityStatus.UPCOMING;
    
    // Denormalized for display
    private String destinationName;
    private String destinationCategory;
    private double destinationLat;
    private double destinationLon;
    private double destinationRating;
    private String destinationImageUrl;
    
    // Travel from previous activity
    private double travelDistanceKm;
    private int travelTimeMinutes;
}
