package com.travelplanner.user.domain;

import com.pgvector.PGvector;
import com.travelplanner.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "user_preferences")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> tagWeights;

    private BigDecimal minBudget;

    private BigDecimal maxBudget;

    private String travelStyle;

    private String groupType;

    @Column(columnDefinition = "vector(384)")
    private PGvector preferenceVector;
}
