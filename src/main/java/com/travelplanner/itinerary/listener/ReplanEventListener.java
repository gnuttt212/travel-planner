package com.travelplanner.itinerary.listener;

import com.travelplanner.itinerary.domain.ReplanSuggestion;
import com.travelplanner.itinerary.event.UserDelayReportedEvent;
import com.travelplanner.itinerary.event.WeatherChangeDetectedEvent;
import com.travelplanner.itinerary.service.AdaptiveReplanService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplanEventListener {

    private final AdaptiveReplanService adaptiveReplanService;

    @Async
    @EventListener
    public void onWeatherChange(WeatherChangeDetectedEvent event) {
        adaptiveReplanService.triggerReplan(
                event.getTripId(),
                ReplanSuggestion.TriggerReason.WEATHER_CHANGE,
                event.getDetail()
        );
    }

    @Async
    @EventListener
    public void onUserDelay(UserDelayReportedEvent event) {
        adaptiveReplanService.triggerReplan(
                event.getTripId(),
                ReplanSuggestion.TriggerReason.USER_DELAY,
                "Trễ " + event.getDelayMinutes() + " phút"
        );
    }
}
