package com.travelplanner.planning.security;

import com.travelplanner.identity.repository.FriendshipRepository;
import com.travelplanner.planning.domain.Trip;
import com.travelplanner.planning.domain.TripVisibility;
import com.travelplanner.planning.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bean tập trung kiểm tra authorization cho Planning module (Trip).
 *
 * Tuân thủ Single Responsibility: chỉ chịu trách nhiệm cho authorization logic
 * của trip, KHÔNG xử lý business logic hay persistence.
 *
 * Các controller và security bean khác (MessagingSecurity) gọi vào đây thay vì
 * tự lặp lại logic if/else → tuân thủ DRY.
 *
 * Sử dụng qua @PreAuthorize:
 *   @PreAuthorize("@tripSecurity.isOwner(#id, authentication.name) or hasRole('ADMIN')")
 * Hoặc gọi trực tiếp trong controller khi cần custom response.
 */
@Component("tripSecurity")
@RequiredArgsConstructor
public class TripSecurity {

    private final TripRepository tripRepository;
    private final FriendshipRepository friendshipRepository;

    /**
     * Kiểm tra user có phải owner của trip hay không.
     * Dùng cho: DELETE /api/v1/trips/{id} (qua @PreAuthorize)
     */
    public boolean isOwner(UUID tripId, String userEmail) {
        return tripRepository.findById(tripId)
                .map(t -> userEmail != null && userEmail.equals(t.getOwnerId()))
                .orElse(false);
    }

    /**
     * Kiểm tra user có quyền xem trip hay không (bao gồm ADMIN bypass).
     * Logic:
     *   1. ADMIN → luôn được xem
     *   2. Owner → luôn được xem trip của mình
     *   3. PUBLIC trip → ai cũng xem được
     *   4. FRIENDS_ONLY trip → chỉ bạn bè của owner mới xem được
     *   5. PRIVATE trip → chỉ owner mới xem được
     *   6. Trip không tồn tại → false (để controller throw ResourceNotFoundException riêng)
     *
     * Dùng cho: GET /trips/{id}, export ICS/PDF, comment checks
     */
    public boolean canView(UUID tripId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        return canViewInternal(tripId, authentication.getName());
    }

    /**
     * Overload dùng cho MessagingSecurity khi tripId là String (Comment.tripId là String).
     * Trả false nếu tripId không parse được thành UUID (trip không hợp lệ).
     */
    public boolean canViewByStringId(String tripId, String userEmail) {
        try {
            UUID uuid = UUID.fromString(tripId);
            return canViewInternal(uuid, userEmail);
        } catch (IllegalArgumentException e) {
            // tripId không phải UUID hợp lệ → không có trip → không có quyền
            return false;
        }
    }

    /**
     * Trả về danh sách trip mà viewer được phép xem từ ownerEmail.
     * Logic:
     *   - Viewer là chính owner → trả tất cả
     *   - Viewer là bạn bè → trả PUBLIC + FRIENDS_ONLY
     *   - Viewer là người lạ → chỉ trả PUBLIC
     *   - ADMIN → trả tất cả
     *
     * Dùng cho: GET /api/v1/users/{email}/trips
     */
    public List<Trip> getVisibleTrips(String ownerEmail, String viewerEmail, Authentication authentication) {
        List<Trip> allTrips = tripRepository.findByOwnerIdOrderByCreatedAtDesc(ownerEmail);

        // ADMIN hoặc chính owner → thấy tất cả
        if (isAdmin(authentication) || ownerEmail.equalsIgnoreCase(viewerEmail)) {
            return allTrips;
        }

        boolean isFriend = friendshipRepository.findByUserIdAndFriendId(viewerEmail, ownerEmail).isPresent();

        return allTrips.stream()
                .filter(trip -> {
                    TripVisibility visibility = trip.getVisibility();
                    if (visibility == null) {
                        // Backward-compatible: trip cũ chưa có visibility → coi như PRIVATE
                        return false;
                    }
                    return switch (visibility) {
                        case PUBLIC -> true;
                        case FRIENDS_ONLY -> isFriend;
                        case PRIVATE -> false;
                    };
                })
                .collect(Collectors.toList());
    }

    // ---- Private helpers ----

    private boolean canViewInternal(UUID tripId, String userEmail) {
        Optional<Trip> optTrip = tripRepository.findById(tripId);
        if (optTrip.isEmpty()) {
            return false;
        }

        Trip trip = optTrip.get();

        // Owner luôn được xem trip của mình
        if (userEmail != null && userEmail.equals(trip.getOwnerId())) {
            return true;
        }

        TripVisibility visibility = trip.getVisibility();
        if (visibility == null) {
            // Trip cũ chưa có visibility → coi như PRIVATE
            return false;
        }

        return switch (visibility) {
            case PUBLIC -> true;
            case FRIENDS_ONLY ->
                    friendshipRepository.findByUserIdAndFriendId(userEmail, trip.getOwnerId()).isPresent();
            case PRIVATE -> false;
        };
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
