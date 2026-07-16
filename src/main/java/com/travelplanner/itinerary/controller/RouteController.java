package com.travelplanner.itinerary.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.itinerary.dto.OptimizedRouteResponse;
import com.travelplanner.itinerary.dto.WaypointDto;
import com.travelplanner.itinerary.service.NominatimService;
import com.travelplanner.itinerary.service.OpenRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final OpenRouteService openRouteService;
    private final NominatimService nominatimService;

    @PostMapping("/optimize")
    public ApiResponse<OptimizedRouteResponse> optimizeRoute(@RequestBody List<WaypointDto> waypoints) {
        OptimizedRouteResponse response = openRouteService.optimizeRoute(waypoints);
        return ApiResponse.success(response);
    }

    @GetMapping("/geocode")
    public ApiResponse<double[]> geocodeLocation(@RequestParam String query) {
        double[] coordinates = nominatimService.geocode(query);
        if (coordinates != null) {
            return ApiResponse.success(coordinates);
        } else {
            return ApiResponse.error(404, "Location not found");
        }
    }
}
