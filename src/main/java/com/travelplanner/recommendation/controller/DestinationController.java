package com.travelplanner.recommendation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.recommendation.repository.DestinationRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
public class DestinationController {
    private final DestinationRepository destinationRepository;

    @GetMapping("/cities")
    public ApiResponse<List<String>> getCities() {
        return ApiResponse.success(destinationRepository.findDistinctCities());
    }
}
