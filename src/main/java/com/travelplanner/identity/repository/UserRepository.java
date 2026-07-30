package com.travelplanner.identity.repository;

import com.travelplanner.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByEmailIn(List<String> emails);
    List<User> findAllByEmailNot(String email);
    List<User> findByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String emailQuery, String displayNameQuery);
}
