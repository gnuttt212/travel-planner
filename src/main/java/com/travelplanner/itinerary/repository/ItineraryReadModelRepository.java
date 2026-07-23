package com.travelplanner.itinerary.repository;

import com.travelplanner.itinerary.domain.ItineraryReadModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryReadModelRepository extends JpaRepository<ItineraryReadModel, String> {
}
