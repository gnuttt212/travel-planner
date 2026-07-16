package com.travelplanner.collaboration.domain;

import com.travelplanner.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "trip_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"itinerary_id", "user_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMember extends BaseEntity {

    @Column(name = "itinerary_id", nullable = false)
    private String itineraryId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripRole role;
}
