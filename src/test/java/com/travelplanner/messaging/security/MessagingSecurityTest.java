package com.travelplanner.messaging.security;

import com.travelplanner.identity.domain.Friendship;
import com.travelplanner.identity.repository.FriendshipRepository;
import com.travelplanner.planning.domain.Trip;
import com.travelplanner.planning.domain.TripVisibility;
import com.travelplanner.planning.repository.TripRepository;
import com.travelplanner.planning.security.TripSecurity;
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
 * Unit tests cho MessagingSecurity — kiểm tra authorization logic
 * cho Comment và Message endpoints.
 */
@ExtendWith(MockitoExtension.class)
class MessagingSecurityTest {

    @Mock
    private TripSecurity tripSecurity;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private MessagingSecurity messagingSecurity;

    private static final String USER_EMAIL = "user@test.com";
    private static final String OTHER_EMAIL = "other@test.com";
    private static final String TRIP_ID = UUID.randomUUID().toString();

    private Authentication mockAuthentication(String email, String role) {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(email);
        Collection authorities = List.of(new SimpleGrantedAuthority(role));
        lenient().doReturn(authorities).when(auth).getAuthorities();
        return auth;
    }

    // ==================== canViewTripComments ====================

    @Nested
    @DisplayName("canViewTripComments()")
    class CanViewTripComments {

        @Test
        @DisplayName("User có quyền xem trip → xem comments được")
        void canViewTripComments_ownerTrip_returnsTrue() {
            when(tripSecurity.canViewByStringId(TRIP_ID, USER_EMAIL)).thenReturn(true);
            Authentication auth = mockAuthentication(USER_EMAIL, "ROLE_USER");

            assertThat(messagingSecurity.canViewTripComments(TRIP_ID, auth)).isTrue();
        }

        @Test
        @DisplayName("User không có quyền xem trip private → 403")
        void canViewTripComments_strangerPrivateTrip_returnsFalse() {
            when(tripSecurity.canViewByStringId(TRIP_ID, OTHER_EMAIL)).thenReturn(false);
            Authentication auth = mockAuthentication(OTHER_EMAIL, "ROLE_USER");

            assertThat(messagingSecurity.canViewTripComments(TRIP_ID, auth)).isFalse();
        }

        @Test
        @DisplayName("ADMIN → luôn xem được comments")
        void canViewTripComments_admin_returnsTrue() {
            Authentication auth = mockAuthentication("admin@test.com", "ROLE_ADMIN");

            assertThat(messagingSecurity.canViewTripComments(TRIP_ID, auth)).isTrue();
            // Không cần gọi tripSecurity vì ADMIN bypass
            verify(tripSecurity, never()).canViewByStringId(any(), any());
        }
    }

    // ==================== canComment ====================

    @Nested
    @DisplayName("canComment()")
    class CanComment {

        @Test
        @DisplayName("User có quyền xem trip → comment được")
        void canComment_validTripAndPermission_returnsTrue() {
            when(tripSecurity.canViewByStringId(TRIP_ID, USER_EMAIL)).thenReturn(true);
            Authentication auth = mockAuthentication(USER_EMAIL, "ROLE_USER");

            assertThat(messagingSecurity.canComment(TRIP_ID, auth)).isTrue();
        }

        @Test
        @DisplayName("User không có quyền xem trip → không comment được")
        void canComment_noPermission_returnsFalse() {
            when(tripSecurity.canViewByStringId(TRIP_ID, OTHER_EMAIL)).thenReturn(false);
            Authentication auth = mockAuthentication(OTHER_EMAIL, "ROLE_USER");

            assertThat(messagingSecurity.canComment(TRIP_ID, auth)).isFalse();
        }
    }

    // ==================== areFriends ====================

    @Nested
    @DisplayName("areFriends()")
    class AreFriends {

        @Test
        @DisplayName("Hai user là bạn bè → trả true")
        void areFriends_existingFriendship_returnsTrue() {
            when(friendshipRepository.findByUserIdAndFriendId(USER_EMAIL, OTHER_EMAIL))
                    .thenReturn(Optional.of(new Friendship()));
            Authentication auth = mockAuthentication(USER_EMAIL, "ROLE_USER");

            assertThat(messagingSecurity.areFriends(USER_EMAIL, OTHER_EMAIL, auth)).isTrue();
        }

        @Test
        @DisplayName("Hai user không phải bạn bè → trả false")
        void areFriends_noFriendship_returnsFalse() {
            when(friendshipRepository.findByUserIdAndFriendId(USER_EMAIL, OTHER_EMAIL))
                    .thenReturn(Optional.empty());
            Authentication auth = mockAuthentication(USER_EMAIL, "ROLE_USER");

            assertThat(messagingSecurity.areFriends(USER_EMAIL, OTHER_EMAIL, auth)).isFalse();
        }

        @Test
        @DisplayName("ADMIN → bypass friendship check")
        void areFriends_admin_returnsTrue() {
            Authentication auth = mockAuthentication("admin@test.com", "ROLE_ADMIN");

            assertThat(messagingSecurity.areFriends("admin@test.com", OTHER_EMAIL, auth)).isTrue();
            verify(friendshipRepository, never()).findByUserIdAndFriendId(any(), any());
        }
    }
}
