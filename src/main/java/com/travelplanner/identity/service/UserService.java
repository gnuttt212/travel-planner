package com.travelplanner.identity.service;

import com.travelplanner.identity.domain.User;
import com.travelplanner.identity.dto.ProfileResponse;
import com.travelplanner.identity.dto.UserProfileUpdateRequest;
import com.travelplanner.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));

        return toProfileResponse(user);
    }

    public ProfileResponse updateProfile(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));

        user.setDisplayName(request.displayName());
        user.setAvatarUrl(request.avatarUrl());
        user.setBio(request.bio());
        userRepository.save(user);

        return toProfileResponse(user);
    }

    private ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio()
        );
    }
}
