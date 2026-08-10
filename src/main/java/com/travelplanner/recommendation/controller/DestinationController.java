package com.travelplanner.recommendation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.recommendation.dto.DestinationSearchRequest;
import com.travelplanner.recommendation.dto.DestinationSearchResponse;
import com.travelplanner.recommendation.repository.DestinationRepository;
import com.travelplanner.recommendation.service.DestinationSearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
public class DestinationController {
    private final DestinationRepository destinationRepository;
    private final DestinationSearchService destinationSearchService;

    @GetMapping("/cities")
    public ApiResponse<List<String>> getCities() {
        return ApiResponse.success(destinationRepository.findDistinctCities());
    }

    @PostMapping("/search")
    public ApiResponse<DestinationSearchResponse> searchDestinations(@RequestBody DestinationSearchRequest request) {
        return ApiResponse.success(destinationSearchService.search(request));
    }
}
