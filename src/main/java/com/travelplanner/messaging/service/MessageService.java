package com.travelplanner.messaging.service;

import com.travelplanner.messaging.domain.Message;
import com.travelplanner.messaging.dto.MessageDto;
import com.travelplanner.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    @Transactional
    public MessageDto sendMessage(String senderEmail, String receiverEmail, String content) {
        Message m = Message.builder()
                .id(UUID.randomUUID().toString())
                .senderEmail(senderEmail)
                .receiverEmail(receiverEmail)
                .content(content)
                .createdAt(Instant.now())
                .build();
        Message saved = messageRepository.save(m);
        return new MessageDto(saved.getId(), saved.getSenderEmail(), saved.getReceiverEmail(), saved.getContent(), saved.getCreatedAt());
    }

    public List<MessageDto> getConversation(String userA, String userB) {
        List<Message> messages = messageRepository.findAllBySenderEmailAndReceiverEmailOrSenderEmailAndReceiverEmailOrderByCreatedAtAsc(
                userA, userB, userB, userA
        );
        return messages.stream().map(msg -> new MessageDto(msg.getId(), msg.getSenderEmail(), msg.getReceiverEmail(), msg.getContent(), msg.getCreatedAt())).collect(Collectors.toList());
    }
}
