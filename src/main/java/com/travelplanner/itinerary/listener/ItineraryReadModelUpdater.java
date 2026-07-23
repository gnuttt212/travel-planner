package com.travelplanner.itinerary.listener;

import com.travelplanner.common.event.ItineraryUpdatedEvent;
import com.travelplanner.itinerary.domain.ItineraryReadModel;
import com.travelplanner.itinerary.repository.ItineraryReadModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItineraryReadModelUpdater {

    private final ItineraryReadModelRepository readModelRepository;

    @EventListener
    public void updateReadModel(ItineraryUpdatedEvent event) {
        log.info("Updating CQRS read model for itinerary {}", event.itineraryId());
        
        ItineraryReadModel readModel = readModelRepository.findById(event.itineraryId())
                .orElseGet(() -> ItineraryReadModel.builder().itineraryId(event.itineraryId()).build());
                
        // For now, the denormalized data is just the raw content string. 
        // If the AI starts returning structured JSON for Leaflet, we can map it here.
        readModel.setDenormalizedData(event.content());
        
        readModelRepository.save(readModel);
    }
}
