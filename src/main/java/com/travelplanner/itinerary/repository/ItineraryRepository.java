package com.travelplanner.itinerary.repository;

import com.travelplanner.itinerary.domain.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface ItineraryRepository extends JpaRepository<Itinerary, String> {
}
