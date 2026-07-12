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
}
