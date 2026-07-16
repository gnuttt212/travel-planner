package com.travelplanner.booking.controller;

import com.travelplanner.booking.domain.BookingRequest;
import com.travelplanner.booking.domain.BookingResult;
import com.travelplanner.booking.service.BookingService;
import com.travelplanner.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResult> book(@RequestBody BookingRequest request) {
        return ApiResponse.success(bookingService.book(request));
    }

    @GetMapping("/providers")
    public ApiResponse<List<String>> getProviders() {
        return ApiResponse.success(bookingService.getAvailableProviders());
    }
}
