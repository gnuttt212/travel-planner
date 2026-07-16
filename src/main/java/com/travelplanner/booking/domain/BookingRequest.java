package com.travelplanner.booking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String providerType; // FLIGHT, HOTEL
    private String origin;
    private String destination;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
}
