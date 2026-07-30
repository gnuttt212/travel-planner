package com.travelplanner.messaging.security;

import com.travelplanner.identity.repository.FriendshipRepository;
import com.travelplanner.planning.security.TripSecurity;
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

    private final TripSecurity tripSecurity;
    private final FriendshipRepository friendshipRepository;

    /**
     * Kiểm tra user có quyền xem comments của một trip hay không.
     * Logic: delegate sang TripSecurity.canView() — nếu user có quyền xem trip
     * thì cũng có quyền xem comments.
     */
    public boolean canViewTripComments(String tripId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        return tripSecurity.canViewByStringId(tripId, authentication.getName());
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
        return tripSecurity.canViewByStringId(tripId, authentication.getName());
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
        return friendshipRepository.findByUserIdAndFriendId(currentEmail, otherEmail).isPresent();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
