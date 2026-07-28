package com.travelplanner.recommendation.repository;

import com.travelplanner.recommendation.domain.Destination;
import com.travelplanner.recommendation.domain.DestinationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.*;

public interface DestinationRepository extends JpaRepository<Destination, UUID> {
    List<Destination> findByCity(String city);

    @Query("SELECT d FROM Destination d WHERE d.city = :city AND d.avgCostPerPerson <= :maxBudget")
    List<Destination> findCandidates(@Param("city") String city, @Param("maxBudget") BigDecimal maxBudget);

    @Query("SELECT DISTINCT d.city FROM Destination d ORDER BY d.city")
    List<String> findDistinctCities();

    List<Destination> findByCategoryAndCityAndIndoorTrue(DestinationCategory category, String city);
}
