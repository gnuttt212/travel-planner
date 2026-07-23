package com.travelplanner.itinerary.scheduler;

import com.travelplanner.itinerary.event.WeatherChangeDetectedEvent;
import com.travelplanner.itinerary.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherMonitorScheduler {

    private final ItineraryService itineraryService;
    private final ApplicationEventPublisher eventPublisher;
    // private final WeatherService weatherService;

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void checkWeatherForActiveTrips() {
        log.info("Running WeatherMonitorScheduler to check for severe weather changes...");
        // ... logic check thời tiết như cũ ...
        // Thay vì gọi adaptiveReplanService.triggerReplan(...) trực tiếp:
        
        // Mock data for compilation
        UUID tripId = UUID.randomUUID();
        String detail = "Typhoon Warning";
        eventPublisher.publishEvent(new WeatherChangeDetectedEvent(tripId, detail));
    }
}
