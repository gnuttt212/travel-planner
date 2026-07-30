package com.travelplanner.identity.dto;

public record FriendRequestDto(
        String id,
        String senderEmail,
        String receiverEmail,
        String status
) {
}
