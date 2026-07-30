package com.travelplanner.identity.repository;

import com.travelplanner.identity.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, String> {
    List<Friendship> findAllByUserId(String userId);
    Optional<Friendship> findByUserIdAndFriendId(String userId, String friendId);
    void deleteByUserIdAndFriendId(String userId, String friendId);
}
