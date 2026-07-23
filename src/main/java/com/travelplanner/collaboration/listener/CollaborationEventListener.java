package com.travelplanner.collaboration.listener;

import com.travelplanner.collaboration.dto.ItineraryEditMessage;
import com.travelplanner.common.event.ItineraryUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollaborationEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onItineraryUpdated(ItineraryUpdatedEvent event) {
        log.info("Received ItineraryUpdatedEvent for itinerary: {}, action: {}", event.itineraryId(), event.action());
        
        ItineraryEditMessage message = new ItineraryEditMessage();
        message.setItineraryId(event.itineraryId());
        message.setAction(event.action());
        message.setUserId("SYSTEM"); // Automatically generated updates
        message.setEntityType("ITINERARY");
        
        messagingTemplate.convertAndSend("/topic/itinerary/" + event.itineraryId(), message);
    }
}
