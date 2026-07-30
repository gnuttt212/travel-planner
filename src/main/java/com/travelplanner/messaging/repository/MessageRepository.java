package com.travelplanner.messaging.repository;

import com.travelplanner.messaging.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findAllBySenderEmailAndReceiverEmailOrderByCreatedAtAsc(String sender, String receiver);
    List<Message> findAllByReceiverEmailAndSenderEmailOrderByCreatedAtAsc(String receiver, String sender);
    List<Message> findAllBySenderEmailAndReceiverEmailOrSenderEmailAndReceiverEmailOrderByCreatedAtAsc(
            String s1, String r1, String s2, String r2);
}
