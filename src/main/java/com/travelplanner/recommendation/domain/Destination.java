package com.travelplanner.recommendation.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "destinations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Destination {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String city;
    private String address;
    
    @Enumerated(EnumType.STRING)
    private DestinationCategory category;
    
    private double latitude;
    private double longitude;
    
    private double avgRating;
    private int reviewCount;
    private BigDecimal avgCostPerPerson;
    
    @Column(columnDefinition = "TEXT")
    private String tags; // JSON array string: ["coffee","wifi"]
    
    @Column(columnDefinition = "TEXT")
    private String openingHours; // JSON: {"mon":"08:00-22:00",...}
    
    @Column(name = "is_indoor")
    private boolean indoor;
    
    private String imageUrl;
    
    @Column(columnDefinition = "TEXT")
    private String bestMonths; // JSON array: [1,2,3,11,12]
    
    public List<String> getTagList() {
        if (tags == null || tags.isEmpty()) return List.of();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                tags, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) { return List.of(); }
    }
    
    public List<Integer> getBestMonthList() {
        if (bestMonths == null || bestMonths.isEmpty()) return List.of();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                bestMonths, new com.fasterxml.jackson.core.type.TypeReference<List<Integer>>() {});
        } catch (Exception e) { return List.of(); }
    }
}
