package com.travelplanner.identity.dto;

public record UserProfileUpdateRequest(
        String displayName,
        String avatarUrl,
        String bio
) {
}
