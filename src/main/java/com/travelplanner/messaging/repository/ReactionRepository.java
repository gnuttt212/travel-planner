package com.travelplanner.messaging.repository;

import com.travelplanner.messaging.domain.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, String> {
    List<Reaction> findAllByTargetTypeAndTargetId(String targetType, String targetId);
    Optional<Reaction> findByAuthorEmailAndTargetTypeAndTargetId(String author, String targetType, String targetId);
}
