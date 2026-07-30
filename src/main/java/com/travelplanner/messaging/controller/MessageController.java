package com.travelplanner.messaging.controller;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.messaging.dto.MessageDto;
import com.travelplanner.messaging.security.MessagingSecurity;
import com.travelplanner.messaging.service.MessageService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final MessagingSecurity messagingSecurity;

    public record SendMessageRequest(@NotBlank String to, @NotBlank String content) {}

    @PostMapping
    public ApiResponse<MessageDto> sendMessage(Authentication authentication, @RequestBody SendMessageRequest req) {
        String from = authentication.getName();
        // Kiểm tra 2 người phải là bạn bè mới được gửi tin nhắn
        if (!messagingSecurity.areFriends(from, req.to(), authentication)) {
            throw new AccessDeniedException("Bạn chỉ có thể gửi tin nhắn cho bạn bè.");
        }
        MessageDto dto = messageService.sendMessage(from, req.to(), req.content());
        return ApiResponse.success(dto);
    }

    @GetMapping("/with/{otherEmail}")
    public ApiResponse<List<MessageDto>> conversation(Authentication authentication, @PathVariable String otherEmail) {
        String me = authentication.getName();
        // Kiểm tra 2 người phải là bạn bè mới được xem cuộc trò chuyện
        if (!messagingSecurity.areFriends(me, otherEmail, authentication)) {
            throw new AccessDeniedException("Bạn chỉ có thể xem tin nhắn với bạn bè.");
        }
        return ApiResponse.success(messageService.getConversation(me, otherEmail));
    }
}
