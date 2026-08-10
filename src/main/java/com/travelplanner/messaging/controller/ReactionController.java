package com.travelplanner.messaging.controller;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.messaging.dto.ReactionDto;
import com.travelplanner.messaging.security.MessagingSecurity;
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
    private final MessagingSecurity messagingSecurity;

    public record ReactRequest(@NotBlank String targetType, @NotBlank String targetId, @NotBlank String type) {}

    @PostMapping
    public ApiResponse<ReactionDto> react(Authentication authentication, @RequestBody ReactRequest req) {
        // Kiểm tra quyền xem target trước khi cho phép thả reaction
        if (!messagingSecurity.canViewReactionTarget(req.targetType(), req.targetId(), authentication)) {
            throw AccessDeniedException.of("target");
        }
        String author = authentication.getName();
        return ApiResponse.success(reactionService.react(author, req.targetType(), req.targetId(), req.type()));
    }

    @DeleteMapping
    public ApiResponse<Void> removeReaction(Authentication authentication, @RequestParam String targetType, @RequestParam String targetId) {
        // Đồng bộ check quyền xem target (kể cả khi đã an toàn về xóa data)
        if (!messagingSecurity.canViewReactionTarget(targetType, targetId, authentication)) {
            throw AccessDeniedException.of("target");
        }
        String author = authentication.getName();
        reactionService.removeReaction(author, targetType, targetId);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<ReactionDto>> list(Authentication authentication, @RequestParam String targetType, @RequestParam String targetId) {
        // Chặn user lạ xem danh sách reaction của một Trip/Comment private
        if (!messagingSecurity.canViewReactionTarget(targetType, targetId, authentication)) {
            throw AccessDeniedException.of("target");
        }
        return ApiResponse.success(reactionService.listForTarget(targetType, targetId));
    }
}
