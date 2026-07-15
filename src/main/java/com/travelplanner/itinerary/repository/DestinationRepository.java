package com.travelplanner.itinerary.repository;

import com.travelplanner.itinerary.domain.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, String> {

    /**
     * Loc cung theo ngan sach/ngay va thang du dinh di - buoc "Loc theo
     * rang buoc cung" trong pipeline goi y da thiet ke o phan truoc.
     * Ket qua duoc sap xep theo popularityScore giam dan.
     */
    @Query("""
            SELECT d FROM Destination d
            WHERE d.avgCostPerDay <= :maxBudgetPerDay
            AND :month MEMBER OF d.bestMonths
            ORDER BY d.popularityScore DESC
            """)
    List<Destination> findMatchingDestinations(
            @Param("maxBudgetPerDay") BigDecimal maxBudgetPerDay,
            @Param("month") Integer month
    );

    /**
     * Native query de ho tro toan tu <-> cua pgvector va tinh Bayesian Average.
     */
    @Query(value = """
            SELECT d.* FROM destinations d
            JOIN destination_best_months dbm ON d.id = dbm.destination_id
            WHERE d.avg_cost_per_day <= :maxBudgetPerDay
            AND dbm.best_months = :month
            ORDER BY (d.embedding <-> CAST(:userVector AS vector)) ASC,
            ((:C * :m + d.review_count * d.rating_average) / (:C + d.review_count)) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Destination> findRecommendedDestinationsNative(
            @Param("maxBudgetPerDay") BigDecimal maxBudgetPerDay,
            @Param("month") Integer month,
            @Param("userVector") String userVector,
            @Param("C") double C,
            @Param("m") double m,
            @Param("limit") int limit
    );
}
