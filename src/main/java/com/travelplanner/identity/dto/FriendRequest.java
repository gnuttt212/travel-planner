package com.travelplanner.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FriendRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email
) {
}
