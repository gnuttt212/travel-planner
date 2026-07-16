package com.travelplanner.booking.provider;

import com.travelplanner.booking.domain.BookingProvider;
import com.travelplanner.booking.domain.BookingRequest;
import com.travelplanner.booking.domain.BookingResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock implementation cua BookingProvider cho dich vu bay.
 * Trong thuc te, day se goi API cua Amadeus, Skyscanner, Traveloka...
 */
@Component
public class FakeFlightProvider implements BookingProvider {

    @Override
    public BookingResult book(BookingRequest request) {
        // Simulate booking logic
        return BookingResult.builder()
                .confirmationCode("FL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .providerType("FLIGHT")
                .status("CONFIRMED")
                .price(BigDecimal.valueOf(2500000 + Math.random() * 3000000))
                .currency("VND")
                .description(String.format("Chuyen bay tu %s den %s ngay %s, %d hanh khach",
                        request.getOrigin(), request.getDestination(),
                        request.getCheckIn(), request.getGuests()))
                .build();
    }

    @Override
    public String getProviderType() {
        return "FLIGHT";
    }
}
