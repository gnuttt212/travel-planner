package com.travelplanner.identity.service;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.common.exception.ResourceNotFoundException;
import com.travelplanner.identity.domain.FriendRequest;
import com.travelplanner.identity.domain.FriendRequestStatus;
import com.travelplanner.identity.domain.Friendship;
import com.travelplanner.identity.domain.User;
import com.travelplanner.identity.dto.FriendRequestDto;
import com.travelplanner.identity.dto.UserDto;
import com.travelplanner.identity.dto.UserSearchResult;
import com.travelplanner.identity.repository.FriendRequestRepository;
import com.travelplanner.identity.repository.FriendshipRepository;
import com.travelplanner.identity.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public List<UserSearchResult> getFriends(String userEmail) {
        List<String> friendEmails = friendshipRepository.findAllByUserId(userEmail).stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        if (friendEmails.isEmpty()) {
            return List.of();
        }

        return userRepository.findByEmailIn(friendEmails).stream()
                .map(user -> new UserSearchResult(user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), "FRIEND"))
                .collect(Collectors.toList());
    }

    public List<UserSearchResult> searchUsers(String query, String currentUserEmail) {
        List<User> users = userRepository.findByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(query, query);

        return users.stream()
                .filter(user -> !user.getEmail().equalsIgnoreCase(currentUserEmail))
                .map(user -> {
                    String relationshipStatus = getRelationshipStatus(currentUserEmail, user.getEmail());
                    return new UserSearchResult(
                            user.getEmail(),
                            user.getDisplayName(),
                            user.getAvatarUrl(),
                            relationshipStatus
                    );
                })
                .collect(Collectors.toList());
    }

    public List<FriendRequestDto> getIncomingRequests(String userEmail) {
        return friendRequestRepository.findAllByReceiverEmailAndStatus(userEmail, FriendRequestStatus.PENDING).stream()
                .map(request -> new FriendRequestDto(request.getId(), request.getSenderEmail(), request.getReceiverEmail(), request.getStatus().name()))
                .collect(Collectors.toList());
    }

    public List<FriendRequestDto> getOutgoingRequests(String userEmail) {
        return friendRequestRepository.findAllBySenderEmailAndStatus(userEmail, FriendRequestStatus.PENDING).stream()
                .map(request -> new FriendRequestDto(request.getId(), request.getSenderEmail(), request.getReceiverEmail(), request.getStatus().name()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendFriendRequest(String senderEmail, String receiverEmail) {
        if (senderEmail.equalsIgnoreCase(receiverEmail)) {
            throw new IllegalArgumentException("Không thể gửi lời mời kết bạn với chính bạn.");
        }

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (friendshipRepository.findByUserIdAndFriendId(senderEmail, receiver.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Bạn đã là bạn bè.");
        }

        friendRequestRepository.findBySenderEmailAndReceiverEmail(senderEmail, receiver.getEmail())
                .ifPresent(request -> {
                    if (request.getStatus() == FriendRequestStatus.PENDING) {
                        throw new IllegalArgumentException("Lời mời kết bạn đã được gửi.");
                    }
                });

        FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.builder()
                .senderEmail(senderEmail)
                .receiverEmail(receiver.getEmail())
                .status(FriendRequestStatus.PENDING)
                .build());

        sendNotification(receiver.getEmail(), new FriendRequestDto(friendRequest.getId(), friendRequest.getSenderEmail(), friendRequest.getReceiverEmail(), friendRequest.getStatus().name()));
    }

    @Transactional
    public void respondToFriendRequest(String receiverEmail, String requestId, boolean accept) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lời mời kết bạn không tồn tại."));

        // Chỉ receiver mới có quyền accept/reject → 403 Forbidden (không phải 400)
        if (!request.getReceiverEmail().equalsIgnoreCase(receiverEmail)) {
            throw new AccessDeniedException("Bạn không có quyền xử lý lời mời kết bạn này.");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Lời mời đã được xử lý.");
        }

        request.setStatus(accept ? FriendRequestStatus.ACCEPTED : FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);

        if (accept) {
            friendshipRepository.save(Friendship.builder()
                    .userId(receiverEmail)
                    .friendId(request.getSenderEmail())
                    .build());
            friendshipRepository.save(Friendship.builder()
                    .userId(request.getSenderEmail())
                    .friendId(receiverEmail)
                    .build());
        }

        sendNotification(request.getSenderEmail(), new FriendRequestDto(request.getId(), request.getSenderEmail(), request.getReceiverEmail(), request.getStatus().name()));
    }

    @Transactional
    public void removeFriend(String userEmail, String friendEmail) {
        friendshipRepository.deleteByUserIdAndFriendId(userEmail, friendEmail);
        friendshipRepository.deleteByUserIdAndFriendId(friendEmail, userEmail);
    }

    /**
     * Kiểm tra hai user có phải là bạn bè hay không.
     * Method public để các module khác (Messaging) sử dụng.
     */
    public boolean areFriends(String emailA, String emailB) {
        return friendshipRepository.findByUserIdAndFriendId(emailA, emailB).isPresent();
    }

    public SseEmitter subscribeFriendRequests(String email) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.computeIfAbsent(email, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitters.getOrDefault(email, new CopyOnWriteArrayList<>()).remove(emitter));
        emitter.onTimeout(() -> emitters.getOrDefault(email, new CopyOnWriteArrayList<>()).remove(emitter));
        emitter.onError((ex) -> emitters.getOrDefault(email, new CopyOnWriteArrayList<>()).remove(emitter));

        return emitter;
    }

    private void sendNotification(String receiverEmail, FriendRequestDto payload) {
        List<SseEmitter> userEmitters = emitters.getOrDefault(receiverEmail, new CopyOnWriteArrayList<>());
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name("friend-request").data(payload));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    private String getRelationshipStatus(String currentUserEmail, String otherEmail) {
        if (friendshipRepository.findByUserIdAndFriendId(currentUserEmail, otherEmail).isPresent()) {
            return "FRIEND";
        }
        if (friendRequestRepository.findBySenderEmailAndReceiverEmail(currentUserEmail, otherEmail).isPresent()) {
            return "REQUEST_SENT";
        }
        if (friendRequestRepository.findBySenderEmailAndReceiverEmail(otherEmail, currentUserEmail).isPresent()) {
            return "REQUEST_RECEIVED";
        }
        return "NONE";
    }
}
