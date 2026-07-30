package com.travelplanner.identity.dto;

import java.time.Instant;

public record ProfileResponse(
        String email,
        String role,
        Instant createdAt,
        String displayName,
        String avatarUrl,
        String bio
) {
}
