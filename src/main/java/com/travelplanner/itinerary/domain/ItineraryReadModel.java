package com.travelplanner.itinerary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "itinerary_read_models")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryReadModel {
    
    @Id
    @Column(name = "itinerary_id")
    private String itineraryId;
    
    @Column(name = "denormalized_data", columnDefinition = "TEXT")
    private String denormalizedData;
}
