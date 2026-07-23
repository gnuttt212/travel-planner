package com.travelplanner.booking.api;

import com.travelplanner.booking.domain.BookingRequest;
import com.travelplanner.booking.domain.BookingResult;

import java.util.List;

public interface BookingApi {
    BookingResult book(BookingRequest request);
    List<String> getAvailableProviders();
}
