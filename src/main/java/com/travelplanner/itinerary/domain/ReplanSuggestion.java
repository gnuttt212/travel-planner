package com.travelplanner.itinerary.domain;

import com.travelplanner.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "replan_suggestions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplanSuggestion extends BaseEntity {

    public enum TriggerReason {
        WEATHER_CHANGE,
        USER_DELAY
    }

    @Column(nullable = false)
    private String itineraryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerReason triggerReason; 

    @Column(columnDefinition = "TEXT")
    private String suggestedChanges; // JSON or text describing the new schedule

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, REJECTED
}
