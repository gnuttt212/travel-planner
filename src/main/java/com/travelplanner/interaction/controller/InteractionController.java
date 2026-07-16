package com.travelplanner.interaction.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.interaction.domain.InteractionType;
import com.travelplanner.interaction.domain.UserInteraction;
import com.travelplanner.interaction.repository.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final UserInteractionRepository interactionRepository;

    @PostMapping
    public ApiResponse<Void> recordInteraction(
            Authentication authentication,
            @RequestParam String destinationId,
            @RequestParam InteractionType type
    ) {
        String userId = authentication.getName();
        UserInteraction interaction = UserInteraction.builder()
                .userId(userId)
                .destinationId(destinationId)
                .interactionType(type)
                .weight(type.getWeight())
                .build();
        interactionRepository.save(interaction);
        return ApiResponse.success(null);
    }
}
