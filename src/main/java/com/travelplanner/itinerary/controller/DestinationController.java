package com.travelplanner.itinerary.controller;

import com.travelplanner.common.response.ApiResponse;
import com.travelplanner.itinerary.dto.DestinationRequest;
import com.travelplanner.itinerary.dto.DestinationResponse;
import com.travelplanner.itinerary.service.DestinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller chi lam nhiem vu: nhan request, goi service, tra response.
 * Khong chua logic nghiep vu (logic nam o Service) - tuan thu Single
 * Responsibility Principle.
 */
@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DestinationResponse> create(@Valid @RequestBody DestinationRequest request) {
        return ApiResponse.success("Tao diem den thanh cong", destinationService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DestinationResponse> getById(@PathVariable String id) {
        return ApiResponse.success(destinationService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<DestinationResponse>> getAll() {
        return ApiResponse.success(destinationService.getAll());
    }

    /**
     * Vi du: GET /api/v1/destinations/recommend?maxBudgetPerDay=1000000&travelMonth=8
     */
    @GetMapping("/recommend")
    public ApiResponse<List<DestinationResponse>> recommend(
            Authentication authentication,
            @RequestParam BigDecimal maxBudgetPerDay,
            @RequestParam Integer travelMonth
    ) {
        String userId = authentication.getName();
        return ApiResponse.success(destinationService.recommend(userId, maxBudgetPerDay, travelMonth));
    }

    @PutMapping("/{id}")
    public ApiResponse<DestinationResponse> update(
            @PathVariable String id,
            @Valid @RequestBody DestinationRequest request
    ) {
        return ApiResponse.success("Cap nhat thanh cong", destinationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        destinationService.delete(id);
    }
}
