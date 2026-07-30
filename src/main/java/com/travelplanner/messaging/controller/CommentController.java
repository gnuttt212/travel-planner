package com.travelplanner.messaging.controller;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.messaging.dto.CommentDto;
import com.travelplanner.messaging.security.MessagingSecurity;
import com.travelplanner.messaging.service.CommentService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final MessagingSecurity messagingSecurity;

    public record AddCommentRequest(@NotBlank String tripId, @NotBlank String content) {}

    @PostMapping
    public ApiResponse<CommentDto> addComment(Authentication authentication, @RequestBody AddCommentRequest req) {
        // Kiểm tra trip tồn tại VÀ user có quyền xem trip → mới được comment
        if (!messagingSecurity.canComment(req.tripId(), authentication)) {
            throw AccessDeniedException.of("trip");
        }
        String author = authentication.getName();
        return ApiResponse.success(commentService.addComment(req.tripId(), author, req.content()));
    }

    @GetMapping("/trip/{tripId}")
    public ApiResponse<List<CommentDto>> listComments(@PathVariable String tripId, Authentication authentication) {
        // Kiểm tra user có quyền xem trip → mới được xem comments
        if (!messagingSecurity.canViewTripComments(tripId, authentication)) {
            throw AccessDeniedException.of("trip");
        }
        return ApiResponse.success(commentService.listComments(tripId));
    }
}
