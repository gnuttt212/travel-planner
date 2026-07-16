package com.travelplanner.collaboration.controller;

import com.travelplanner.collaboration.dto.ItineraryEditMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller xu ly viec chinh sua lich trinh theo thoi gian thuc.
 *
 * Flow:
 * 1. Client gui message toi /app/itinerary.edit
 * 2. Server broadcast toi /topic/itinerary/{itineraryId}
 * 3. Tat ca client dang subscribe kenh do se nhan duoc thay doi
 */
@Controller
@Slf4j
public class CollaborationController {

    private final SimpMessagingTemplate messagingTemplate;

    public CollaborationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/itinerary.edit")
    public void handleItineraryEdit(@Payload ItineraryEditMessage message) {
        log.info("Received edit from user {} on itinerary {}: {} {}",
                message.getUserId(), message.getItineraryId(),
                message.getAction(), message.getEntityType());
        
        // Broadcast to all subscribers of this itinerary
        messagingTemplate.convertAndSend(
                "/topic/itinerary/" + message.getItineraryId(),
                message
        );
    }
}
