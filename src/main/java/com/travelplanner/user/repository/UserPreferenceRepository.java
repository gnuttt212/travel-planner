package com.travelplanner.user.repository;

import com.travelplanner.user.domain.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {
    Optional<UserPreference> findByUserId(String userId);

    @Query(value = """
            SELECT user_id FROM user_preferences
            WHERE user_id != :userId
            ORDER BY preference_vector <=> CAST(:userVector AS vector) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findSimilarUsers(@Param("userId") String userId, @Param("userVector") String userVector, @Param("limit") int limit);
}
