package com.travelplanner.messaging.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "reactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {
    @Id
    private String id;

    @Column(nullable = false)
    private String authorEmail;

    @Column(nullable = false)
    private String targetType; // MESSAGE or COMMENT

    @Column(nullable = false)
    private String targetId;

    @Column(nullable = false)
    private String type; // e.g., LIKE, LOVE, LAUGH, SAD

    @Column(nullable = false)
    private Instant createdAt;
}
