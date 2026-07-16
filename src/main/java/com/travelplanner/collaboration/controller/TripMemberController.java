package com.travelplanner.collaboration.controller;

import com.travelplanner.collaboration.domain.TripMember;
import com.travelplanner.collaboration.domain.TripRole;
import com.travelplanner.collaboration.repository.TripMemberRepository;
import com.travelplanner.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripMemberController {

    private final TripMemberRepository tripMemberRepository;

    @PostMapping("/{itineraryId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TripMember> addMember(
            @PathVariable String itineraryId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "VIEWER") TripRole role
    ) {
        TripMember member = TripMember.builder()
                .itineraryId(itineraryId)
                .userId(userId)
                .role(role)
                .build();
        return ApiResponse.success(tripMemberRepository.save(member));
    }

    @GetMapping("/{itineraryId}/members")
    public ApiResponse<List<TripMember>> getMembers(@PathVariable String itineraryId) {
        return ApiResponse.success(tripMemberRepository.findByItineraryId(itineraryId));
    }
}
