package com.travelplanner.interaction.repository;

import com.travelplanner.interaction.domain.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface UserInteractionRepository extends JpaRepository<UserInteraction, String> {

    @Query("SELECT ui.destinationId, SUM(ui.weight) FROM UserInteraction ui GROUP BY ui.destinationId")
    List<Object[]> getAggregatedWeightsPerDestination();

    @Query("""
            SELECT ui.destinationId
            FROM UserInteraction ui
            WHERE ui.userId IN :userIds
            GROUP BY ui.destinationId
            ORDER BY SUM(ui.weight) DESC
            LIMIT :limit
            """)
    List<String> findTopDestinationsBySimilarUsers(@Param("userIds") List<String> userIds, @Param("limit") int limit);
}
