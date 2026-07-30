package com.travelplanner.planning.security;

import com.travelplanner.identity.domain.Friendship;
import com.travelplanner.identity.repository.FriendshipRepository;
import com.travelplanner.planning.domain.Trip;
import com.travelplanner.planning.domain.TripVisibility;
import com.travelplanner.planning.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho TripSecurity — kiểm tra logic phân quyền trip.
 * Mỗi test case kiểm tra một scenario cụ thể: owner, stranger, admin, friend,
 * và các mức visibility (PUBLIC, FRIENDS_ONLY, PRIVATE).
 */
@ExtendWith(MockitoExtension.class)
class TripSecurityTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private TripSecurity tripSecurity;

    private static final UUID TRIP_ID = UUID.randomUUID();
    private static final String OWNER_EMAIL = "owner@test.com";
    private static final String STRANGER_EMAIL = "stranger@test.com";
    private static final String FRIEND_EMAIL = "friend@test.com";

    private Trip buildTrip(TripVisibility visibility) {
        return Trip.builder()
                .id(TRIP_ID)
                .ownerId(OWNER_EMAIL)
                .title("Test Trip")
                .visibility(visibility)
                .build();
    }

    private Authentication mockAuthentication(String email, String role) {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(email);
        Collection authorities = List.of(new SimpleGrantedAuthority(role));
        lenient().doReturn(authorities).when(auth).getAuthorities();
        return auth;
    }

    // ==================== isOwner ====================

    @Nested
    @DisplayName("isOwner()")
    class IsOwner {

        @Test
        @DisplayName("Owner truy cập → trả true")
        void isOwner_ownerAccess_returnsTrue() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(buildTrip(TripVisibility.PRIVATE)));
            assertThat(tripSecurity.isOwner(TRIP_ID, OWNER_EMAIL)).isTrue();
        }

        @Test
        @DisplayName("Stranger truy cập → trả false")
        void isOwner_strangerAccess_returnsFalse() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(buildTrip(TripVisibility.PRIVATE)));
            assertThat(tripSecurity.isOwner(TRIP_ID, STRANGER_EMAIL)).isFalse();
        }

        @Test
        @DisplayName("Trip không tồn tại → trả false")
        void isOwner_tripNotFound_returnsFalse() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());
            assertThat(tripSecurity.isOwner(TRIP_ID, OWNER_EMAIL)).isFalse();
        }
    }

    // ==================== canView ====================

    @Nested
    @DisplayName("canView()")
    class CanView {

        @Test
        @DisplayName("Owner xem trip của mình → pass")
        void canView_owner_returnsTrue() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(buildTrip(TripVisibility.PRIVATE)));
            Authentication auth = mockAuthentication(OWNER_EMAIL, "ROLE_USER");

            assertThat(tripSecurity.canView(TRIP_ID, auth)).isTrue();
        }

        @Test
        @DisplayName("PUBLIC trip → bất kỳ user nào cũng xem được")
        void canView_publicTrip_anyUser_returnsTrue() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(buildTrip(TripVisibility.PUBLIC)));
            Authentication auth = mockAuthentication(STRANGER_EMAIL, "ROLE_USER");

            assertThat(tripSecurity.canView(TRIP_ID, auth)).isTrue();
        }

        @Test
        @DisplayName("FRIENDS_ONLY trip + user là bạn bè → pass")
        void canView_friendsOnlyTrip_friend_returnsTrue() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(buildTrip(TripVisibility.FRIENDS_ONLY)));
            when(friendshipRepository.findByUserIdAndFriendId(FRIEND_EMAIL, OWNER_EMAIL))
                    .thenReturn(Optional.of(new Friendship()));
            Authentication auth = mockAuthentication(FRIEND_EMAIL, "ROLE_USER");

            assertThat(tripSecurity.canView(TRIP_ID, auth)).isTrue();
        }

        @Test
        @DisplayName("FRIENDS_ONLY trip + stranger → 403")
        void canView_friendsOnlyTrip_stranger_returnsFalse() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(buildTrip(TripVisibility.FRIENDS_ONLY)));
            when(friendshipRepository.findByUserIdAndFriendId(STRANGER_EMAIL, OWNER_EMAIL))
                    .thenReturn(Optional.empty());
            Authentication auth = mockAuthentication(STRANGER_EMAIL, "ROLE_USER");

            assertThat(tripSecurity.canView(TRIP_ID, auth)).isFalse();
        }

        @Test
        @DisplayName("PRIVATE trip + stranger → 403")
        void canView_privateTrip_stranger_returnsFalse() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(buildTrip(TripVisibility.PRIVATE)));
            Authentication auth = mockAuthentication(STRANGER_EMAIL, "ROLE_USER");

            assertThat(tripSecurity.canView(TRIP_ID, auth)).isFalse();
        }

        @Test
        @DisplayName("ADMIN luôn xem được mọi trip (bypass)")
        void canView_admin_alwaysTrue() {
            // Không cần mock tripRepository vì ADMIN bypass trước khi query
            Authentication auth = mockAuthentication("admin@test.com", "ROLE_ADMIN");

            assertThat(tripSecurity.canView(TRIP_ID, auth)).isTrue();
        }

        @Test
        @DisplayName("Trip không tồn tại → trả false")
        void canView_tripNotFound_returnsFalse() {
            when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());
            Authentication auth = mockAuthentication(STRANGER_EMAIL, "ROLE_USER");

            assertThat(tripSecurity.canView(TRIP_ID, auth)).isFalse();
        }
    }

    // ==================== getVisibleTrips ====================

    @Nested
    @DisplayName("getVisibleTrips()")
    class GetVisibleTrips {

        private List<Trip> allTrips;

        @BeforeEach
        void setup() {
            allTrips = List.of(
                    buildTripWith("Public Trip", TripVisibility.PUBLIC),
                    buildTripWith("Friends Trip", TripVisibility.FRIENDS_ONLY),
                    buildTripWith("Private Trip", TripVisibility.PRIVATE)
            );
            when(tripRepository.findByOwnerIdOrderByCreatedAtDesc(OWNER_EMAIL)).thenReturn(allTrips);
        }

        private Trip buildTripWith(String title, TripVisibility visibility) {
            return Trip.builder()
                    .id(UUID.randomUUID())
                    .ownerId(OWNER_EMAIL)
                    .title(title)
                    .visibility(visibility)
                    .build();
        }

        @Test
        @DisplayName("Owner xem trip của mình → thấy tất cả")
        void getVisibleTrips_self_returnsAll() {
            Authentication auth = mockAuthentication(OWNER_EMAIL, "ROLE_USER");

            List<Trip> result = tripSecurity.getVisibleTrips(OWNER_EMAIL, OWNER_EMAIL, auth);
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Bạn bè → thấy PUBLIC + FRIENDS_ONLY")
        void getVisibleTrips_friend_returnsPublicAndFriendsOnly() {
            when(friendshipRepository.findByUserIdAndFriendId(FRIEND_EMAIL, OWNER_EMAIL))
                    .thenReturn(Optional.of(new Friendship()));
            Authentication auth = mockAuthentication(FRIEND_EMAIL, "ROLE_USER");

            List<Trip> result = tripSecurity.getVisibleTrips(OWNER_EMAIL, FRIEND_EMAIL, auth);
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Trip::getTitle)
                    .containsExactly("Public Trip", "Friends Trip");
        }

        @Test
        @DisplayName("Người lạ → chỉ thấy PUBLIC")
        void getVisibleTrips_stranger_returnsOnlyPublic() {
            when(friendshipRepository.findByUserIdAndFriendId(STRANGER_EMAIL, OWNER_EMAIL))
                    .thenReturn(Optional.empty());
            Authentication auth = mockAuthentication(STRANGER_EMAIL, "ROLE_USER");

            List<Trip> result = tripSecurity.getVisibleTrips(OWNER_EMAIL, STRANGER_EMAIL, auth);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Public Trip");
        }

        @Test
        @DisplayName("ADMIN → thấy tất cả")
        void getVisibleTrips_admin_returnsAll() {
            Authentication auth = mockAuthentication("admin@test.com", "ROLE_ADMIN");

            List<Trip> result = tripSecurity.getVisibleTrips(OWNER_EMAIL, "admin@test.com", auth);
            assertThat(result).hasSize(3);
        }
    }
}
