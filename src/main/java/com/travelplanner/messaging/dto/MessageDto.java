package com.travelplanner.messaging.dto;

import java.time.Instant;

public record MessageDto(
        String id,
        String senderEmail,
        String receiverEmail,
        String content,
        Instant createdAt
) {}
