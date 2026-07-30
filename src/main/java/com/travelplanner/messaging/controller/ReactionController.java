package com.travelplanner.messaging.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.messaging.dto.ReactionDto;
import com.travelplanner.messaging.service.ReactionService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reactions")
@RequiredArgsConstructor
public class ReactionController {
    private final ReactionService reactionService;

    public record ReactRequest(@NotBlank String targetType, @NotBlank String targetId, @NotBlank String type) {}

    @PostMapping
    public ApiResponse<ReactionDto> react(Authentication authentication, @RequestBody ReactRequest req) {
        String author = authentication.getName();
        return ApiResponse.success(reactionService.react(author, req.targetType(), req.targetId(), req.type()));
    }

    @DeleteMapping
    public ApiResponse<Void> removeReaction(Authentication authentication, @RequestParam String targetType, @RequestParam String targetId) {
        String author = authentication.getName();
        reactionService.removeReaction(author, targetType, targetId);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<ReactionDto>> list(@RequestParam String targetType, @RequestParam String targetId) {
        return ApiResponse.success(reactionService.listForTarget(targetType, targetId));
    }
}
