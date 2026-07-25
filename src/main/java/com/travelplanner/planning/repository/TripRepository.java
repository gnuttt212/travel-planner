package com.travelplanner.planning.repository;

import com.travelplanner.planning.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByOwnerIdOrderByCreatedAtDesc(String ownerId);
}
