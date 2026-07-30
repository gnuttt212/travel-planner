package com.travelplanner.identity.dto;

public record UserSearchResult(
        String email,
        String displayName,
        String avatarUrl,
        String relationshipStatus
) {
}
