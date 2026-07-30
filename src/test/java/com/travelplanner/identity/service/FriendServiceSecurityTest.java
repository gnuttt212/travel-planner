package com.travelplanner.identity.service;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.common.exception.ResourceNotFoundException;
import com.travelplanner.identity.domain.FriendRequest;
import com.travelplanner.identity.domain.FriendRequestStatus;
import com.travelplanner.identity.domain.Friendship;
import com.travelplanner.identity.repository.FriendRequestRepository;
import com.travelplanner.identity.repository.FriendshipRepository;
import com.travelplanner.identity.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho FriendService — tập trung vào security aspects:
 * respondToFriendRequest ownership check và areFriends utility method.
 */
@ExtendWith(MockitoExtension.class)
class FriendServiceSecurityTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendService friendService;

    private static final String SENDER_EMAIL = "sender@test.com";
    private static final String RECEIVER_EMAIL = "receiver@test.com";
    private static final String STRANGER_EMAIL = "stranger@test.com";
    private static final String REQUEST_ID = "req-123";

    private FriendRequest buildPendingRequest() {
        FriendRequest req = new FriendRequest();
        req.setId(REQUEST_ID);
        req.setSenderEmail(SENDER_EMAIL);
        req.setReceiverEmail(RECEIVER_EMAIL);
        req.setStatus(FriendRequestStatus.PENDING);
        return req;
    }

    // ==================== respondToFriendRequest ====================

    @Nested
    @DisplayName("respondToFriendRequest() — authorization")
    class RespondToFriendRequest {

        @Test
        @DisplayName("Receiver accept → thành công, không throw")
        void respondToFriendRequest_receiver_succeeds() {
            FriendRequest req = buildPendingRequest();
            when(friendRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(req));
            when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(req);

            assertThatCode(() -> friendService.respondToFriendRequest(RECEIVER_EMAIL, REQUEST_ID, true))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Sender tự accept request của mình → throw AccessDeniedException (403)")
        void respondToFriendRequest_sender_throwsAccessDenied() {
            FriendRequest req = buildPendingRequest();
            when(friendRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(req));

            assertThatThrownBy(() -> friendService.respondToFriendRequest(SENDER_EMAIL, REQUEST_ID, true))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("không có quyền");
        }

        @Test
        @DisplayName("Stranger xử lý request → throw AccessDeniedException (403)")
        void respondToFriendRequest_stranger_throwsAccessDenied() {
            FriendRequest req = buildPendingRequest();
            when(friendRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(req));

            assertThatThrownBy(() -> friendService.respondToFriendRequest(STRANGER_EMAIL, REQUEST_ID, false))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("không có quyền");
        }

        @Test
        @DisplayName("Request không tồn tại → throw ResourceNotFoundException")
        void respondToFriendRequest_notFound_throwsNotFound() {
            when(friendRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> friendService.respondToFriendRequest(RECEIVER_EMAIL, REQUEST_ID, true))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== areFriends ====================

    @Nested
    @DisplayName("areFriends()")
    class AreFriendsTest {

        @Test
        @DisplayName("Hai user là bạn bè → trả true")
        void areFriends_existingFriendship_returnsTrue() {
            when(friendshipRepository.findByUserIdAndFriendId(SENDER_EMAIL, RECEIVER_EMAIL))
                    .thenReturn(Optional.of(new Friendship()));

            assertThat(friendService.areFriends(SENDER_EMAIL, RECEIVER_EMAIL)).isTrue();
        }

        @Test
        @DisplayName("Hai user không phải bạn bè → trả false")
        void areFriends_noFriendship_returnsFalse() {
            when(friendshipRepository.findByUserIdAndFriendId(SENDER_EMAIL, STRANGER_EMAIL))
                    .thenReturn(Optional.empty());

            assertThat(friendService.areFriends(SENDER_EMAIL, STRANGER_EMAIL)).isFalse();
        }
    }
}
