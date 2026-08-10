package com.travelplanner.messaging.security;

import com.travelplanner.common.port.FriendshipPort;
import com.travelplanner.messaging.repository.CommentRepository;
import com.travelplanner.common.port.TripAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Bean tập trung kiểm tra authorization cho Messaging module (Comment, Message).
 *
 * Tuân thủ Single Responsibility: chỉ chịu trách nhiệm cho authorization logic
 * của messaging, delegate trip-related checks sang TripSecurity.
 *
 * Sử dụng qua @PreAuthorize hoặc gọi trực tiếp trong controller:
 *   @PreAuthorize("@messagingSecurity.areFriends(authentication.name, #otherEmail)")
 */
@Component("messagingSecurity")
@RequiredArgsConstructor
public class MessagingSecurity {

    private final TripAccessPort tripAccessPort;
    private final FriendshipPort friendshipPort;
    private final CommentRepository commentRepository;

    /**
     * Kiểm tra user có quyền xem comments của một trip hay không.
     * Logic: delegate sang TripSecurity.canView() — nếu user có quyền xem trip
     * thì cũng có quyền xem comments.
     */
    public boolean canViewTripComments(String tripId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        return tripAccessPort.canView(tripId, authentication.getName());
    }

    /**
     * Kiểm tra user có quyền comment vào một trip hay không.
     * Yêu cầu: trip phải tồn tại VÀ user phải có quyền xem trip đó.
     * (Nếu không xem được trip thì không thể comment)
     */
    public boolean canComment(String tripId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        return tripAccessPort.canView(tripId, authentication.getName());
    }

    /**
     * Kiểm tra hai user có phải là bạn bè hay không.
     * Dùng cho endpoint GET /api/v1/messages/with/{otherEmail}.
     * ADMIN được bypass check này.
     */
    public boolean areFriends(String currentEmail, String otherEmail, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        return friendshipPort.areFriends(currentEmail, otherEmail);
    }

    /**
     * Kiểm tra user có quyền tương tác với target của reaction hay không.
     * Target có thể là TRIP hoặc COMMENT — nếu là COMMENT thì lấy tripId
     * của comment đó rồi kiểm tra quyền xem trip tương ứng.
     * Dùng cho: POST/GET/DELETE /api/v1/reactions
     */
    public boolean canViewReactionTarget(String targetType, String targetId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        String userEmail = authentication.getName();

        if ("TRIP".equalsIgnoreCase(targetType)) {
            // Target là Trip → check quyền xem trip trực tiếp
            return tripAccessPort.canView(targetId, userEmail);
        } else if ("COMMENT".equalsIgnoreCase(targetType)) {
            // Target là Comment → lấy tripId từ comment rồi check quyền xem trip đó
            return commentRepository.findById(targetId)
                    .map(comment -> tripAccessPort.canView(comment.getTripId(), userEmail))
                    .orElse(false);
        }
        // Loại target không xác định → từ chối
        return false;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}

