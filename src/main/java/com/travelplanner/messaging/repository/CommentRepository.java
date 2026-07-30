package com.travelplanner.messaging.repository;

import com.travelplanner.messaging.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findAllByTripIdOrderByCreatedAtAsc(String tripId);
}
