package com.travelplanner.itinerary.service;

import com.travelplanner.common.event.ItineraryUpdatedEvent;
import com.travelplanner.itinerary.domain.Itinerary;
import com.travelplanner.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Itinerary saveAndPublish(Itinerary itinerary, String action) {
        Itinerary saved = itineraryRepository.save(itinerary);
        eventPublisher.publishEvent(new ItineraryUpdatedEvent(saved.getId(), action, saved.getContent()));
        return saved;
    }
}
