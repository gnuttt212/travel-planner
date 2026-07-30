package com.travelplanner.messaging.dto;

import java.time.Instant;

public record ReactionDto(String id, String authorEmail, String targetType, String targetId, String type, Instant createdAt) {}
