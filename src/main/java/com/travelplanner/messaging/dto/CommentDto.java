package com.travelplanner.messaging.dto;

import java.time.Instant;

public record CommentDto(String id, String tripId, String authorEmail, String content, Instant createdAt) {}
