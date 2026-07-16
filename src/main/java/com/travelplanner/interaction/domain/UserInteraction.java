package com.travelplanner.interaction.domain;

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
@Table(name = "user_interactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInteraction extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String destinationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType interactionType;

    @Column(nullable = false)
    private Double weight;
}
