package com.travelplanner.itinerary.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.itinerary.dto.DelayReportRequest;
import com.travelplanner.itinerary.event.UserDelayReportedEvent;
import com.travelplanner.itinerary.service.AdaptiveReplanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
public class ReplanController {

    private final AdaptiveReplanService adaptiveReplanService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/{tripId}/report-delay")
    public ResponseEntity<ApiResponse<String>> reportDelay(
            @PathVariable UUID tripId,
            @Valid @RequestBody DelayReportRequest request) {
        
        eventPublisher.publishEvent(new UserDelayReportedEvent(tripId, request.delayMinutes()));
        return ResponseEntity.ok(ApiResponse.success("Đang tạo đề xuất điều chỉnh..."));
    }

    @PostMapping("/suggestions/{suggestionId}/resolve")
    public ResponseEntity<ApiResponse<String>> resolveSuggestion(
            @PathVariable UUID suggestionId,
            @RequestBody boolean accepted) {
        adaptiveReplanService.resolveSuggestion(suggestionId, accepted);
        return ResponseEntity.ok(ApiResponse.success("Đã phản hồi đề xuất điều chỉnh"));
    }
}
