package com.travelplanner.itinerary.domain;

import com.travelplanner.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.pgvector.PGvector;

/**
 * Destination = mot diem den co the duoc goi y trong lich trinh.
 *
 * Ghi chu thiet ke: cac truong "tags" va "bestMonths" duoc dung cho buoc
 * loc cung (hard filter) trong pipeline goi y; truong "description" se
 * duoc dung de sinh embedding phuc vu tim kiem ngu nghia (vector search)
 * o giai doan sau (xem them: pgvector extension cua PostgreSQL).
 */
@Getter
@Setter
@Entity
@Table(name = "destinations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destination extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String country;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    private List<String> tags; // vi du: "bien", "am-thuc", "phieu-luu"

    @ElementCollection
    private List<Integer> bestMonths; // vi du: [6, 7, 8] - mua dep de di

    @Column(name = "avg_cost_per_day")
    private BigDecimal avgCostPerDay;

    @Column(name = "popularity_score")
    @Builder.Default
    private Double popularityScore = 0.0;

    @Column(name = "rating_average")
    @Builder.Default
    private Double ratingAverage = 0.0;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(columnDefinition = "vector(384)")
    private PGvector embedding;

    private Double latitude;
    private Double longitude;
}
