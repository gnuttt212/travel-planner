package com.travelplanner.booking.provider;

import com.travelplanner.booking.domain.BookingProvider;
import com.travelplanner.booking.domain.BookingRequest;
import com.travelplanner.booking.domain.BookingResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Mock implementation cua BookingProvider cho dich vu khach san.
 * Trong thuc te, day se goi API cua Booking.com, Agoda, Hotels.com...
 */
@Component
public class FakeHotelProvider implements BookingProvider {

    @Override
    public BookingResult book(BookingRequest request) {
        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        if (nights <= 0) nights = 1;
        
        BigDecimal pricePerNight = BigDecimal.valueOf(800000 + Math.random() * 2000000);
        BigDecimal totalPrice = pricePerNight.multiply(BigDecimal.valueOf(nights));

        return BookingResult.builder()
                .confirmationCode("HT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .providerType("HOTEL")
                .status("CONFIRMED")
                .price(totalPrice)
                .currency("VND")
                .description(String.format("Khach san tai %s, %d dem (%s -> %s), %d khach",
                        request.getDestination(), nights,
                        request.getCheckIn(), request.getCheckOut(), request.getGuests()))
                .build();
    }

    @Override
    public String getProviderType() {
        return "HOTEL";
    }
}
