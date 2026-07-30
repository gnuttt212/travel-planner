package com.travelplanner.identity.repository;

import com.travelplanner.identity.domain.FriendRequest;
import com.travelplanner.identity.domain.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, String> {
    Optional<FriendRequest> findBySenderEmailAndReceiverEmail(String senderEmail, String receiverEmail);
    List<FriendRequest> findAllByReceiverEmailAndStatus(String receiverEmail, FriendRequestStatus status);
    List<FriendRequest> findAllBySenderEmailAndStatus(String senderEmail, FriendRequestStatus status);
    List<FriendRequest> findAllBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail);
}
