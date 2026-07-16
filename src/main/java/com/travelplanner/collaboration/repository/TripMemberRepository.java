package com.travelplanner.collaboration.repository;

import com.travelplanner.collaboration.domain.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, String> {
    List<TripMember> findByItineraryId(String itineraryId);
    List<TripMember> findByUserId(String userId);
    Optional<TripMember> findByItineraryIdAndUserId(String itineraryId, String userId);
}
