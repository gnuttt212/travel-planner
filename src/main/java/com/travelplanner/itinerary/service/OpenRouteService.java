package com.travelplanner.itinerary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.itinerary.dto.OptimizedRouteResponse;
import com.travelplanner.itinerary.dto.WaypointDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service tich hop OpenRouteService (ORS) de toi uu lo trinh (TSP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenRouteService {

    @Value("${ors.api.key}")
    private String apiKey;

    @Value("${ors.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper;

    /**
     * Goi ORS Optimization API (VROOM) de sap xep lai danh sach diem den.
     * Neu that bai hoac khong co API key, dung Nearest Neighbor fallback.
     */
    public OptimizedRouteResponse optimizeRoute(List<WaypointDto> waypoints) {
        if (waypoints == null || waypoints.size() < 2) {
            return new OptimizedRouteResponse(waypoints, 0.0, 0.0);
        }

        if (apiKey == null || apiKey.contains("YOUR_ORS_API_KEY_HERE")) {
            log.warn("No ORS API Key provided. Falling back to Nearest Neighbor algorithm.");
            return nearestNeighborFallback(waypoints);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = apiUrl + "/optimization";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);

            // Xay dung payload cho ORS Optimization
            Map<String, Object> payload = buildOptimizationPayload(waypoints);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            return parseOptimizationResponse(response, waypoints);

        } catch (Exception e) {
            log.error("ORS Optimization failed, falling back to Nearest Neighbor", e);
            return nearestNeighborFallback(waypoints);
        }
    }

    private Map<String, Object> buildOptimizationPayload(List<WaypointDto> waypoints) {
        Map<String, Object> payload = new HashMap<>();

        // Vehicle: Bat dau tu diem dau tien, ket thuc tai diem cuoi cung (hoac de mo)
        WaypointDto startPoint = waypoints.get(0);
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("id", 1);
        vehicle.put("profile", "driving-car");
        vehicle.put("start", new double[]{startPoint.getLongitude(), startPoint.getLatitude()});

        payload.put("vehicles", List.of(vehicle));

        // Jobs: Cac diem den con lai
        List<Map<String, Object>> jobs = new ArrayList<>();
        for (int i = 1; i < waypoints.size(); i++) {
            WaypointDto wp = waypoints.get(i);
            Map<String, Object> job = new HashMap<>();
            job.put("id", i + 1); // Job ID phai duy nhat
            job.put("location", new double[]{wp.getLongitude(), wp.getLatitude()});
            jobs.add(job);
        }
        
        payload.put("jobs", jobs);
        return payload;
    }

    @SuppressWarnings("unchecked")
    private OptimizedRouteResponse parseOptimizationResponse(Map<String, Object> response, List<WaypointDto> originalWaypoints) {
        if (response == null || !response.containsKey("summary")) {
            return nearestNeighborFallback(originalWaypoints);
        }

        Map<String, Object> summary = (Map<String, Object>) response.get("summary");
        Map<String, Object> unassigned = (Map<String, Object>) response.get("unassigned");
        
        double distanceKm = 0.0;
        double durationMins = 0.0;
        
        if (summary != null) {
            // ORS tra ve distance theo met, duration theo giay
            int distance = (int) summary.getOrDefault("distance", 0);
            int duration = (int) summary.getOrDefault("duration", 0);
            distanceKm = distance / 1000.0;
            durationMins = duration / 60.0;
        }

        // De don gian trong demo, ta gia dinh ORS sap xep thanh cong va phuc hoi thu tu.
        // Trong thuc te, can doc mang "routes" -> "steps" de lay ID.
        // O day minh van fallback luon neu co loi de dam bao an toan
        List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
        if (routes != null && !routes.isEmpty()) {
            List<Map<String, Object>> steps = (List<Map<String, Object>>) routes.get(0).get("steps");
            List<WaypointDto> optimized = new ArrayList<>();
            
            for (Map<String, Object> step : steps) {
                String type = (String) step.get("type");
                if ("start".equals(type)) {
                    optimized.add(originalWaypoints.get(0));
                } else if ("job".equals(type)) {
                    int jobId = (int) step.get("job");
                    optimized.add(originalWaypoints.get(jobId - 1));
                }
            }
            return new OptimizedRouteResponse(optimized, distanceKm, durationMins);
        }

        return nearestNeighborFallback(originalWaypoints);
    }

    /**
     * Fallback Algorithm: Nearest Neighbor su dung cong thuc Haversine
     */
    private OptimizedRouteResponse nearestNeighborFallback(List<WaypointDto> waypoints) {
        List<WaypointDto> unvisited = new ArrayList<>(waypoints);
        List<WaypointDto> optimized = new ArrayList<>();
        
        WaypointDto current = unvisited.remove(0);
        optimized.add(current);
        
        double totalDistance = 0.0;

        while (!unvisited.isEmpty()) {
            WaypointDto nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (WaypointDto wp : unvisited) {
                double dist = haversine(current.getLatitude(), current.getLongitude(), wp.getLatitude(), wp.getLongitude());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = wp;
                }
            }

            totalDistance += minDistance;
            optimized.add(nearest);
            unvisited.remove(nearest);
            current = nearest;
        }

        return new OptimizedRouteResponse(optimized, totalDistance, totalDistance * 1.5); // Estimate 1.5 min per km
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
