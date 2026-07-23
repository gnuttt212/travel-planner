package com.travelplanner.itinerary.repository;

import com.travelplanner.itinerary.domain.ReplanSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplanSuggestionRepository extends JpaRepository<ReplanSuggestion, String> {
    List<ReplanSuggestion> findByItineraryId(String itineraryId);
    List<ReplanSuggestion> findByItineraryIdAndStatus(String itineraryId, String status);
}
