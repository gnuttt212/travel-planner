package com.travelplanner.planning.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "trips")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Trip {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String ownerId;
    private String title;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TripStatus status;
    
    private LocalDate tripDate;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TripDuration duration;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TripPurpose purpose;
    
    private int groupSize;
    private double startLat;
    private double startLon;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Transportation transportation;

    /** Mức độ hiển thị: PUBLIC / FRIENDS_ONLY / PRIVATE. Default PRIVATE cho backward-compatible. */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private TripVisibility visibility = TripVisibility.PRIVATE;
    
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex")
    @Builder.Default
    private List<TripActivity> activities = new ArrayList<>();
    
    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant updatedAt;
}
