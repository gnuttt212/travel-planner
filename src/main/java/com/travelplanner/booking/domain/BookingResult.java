package com.travelplanner.booking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResult {
    private String confirmationCode;
    private String providerType;
    private String status;
    private BigDecimal price;
    private String currency;
    private String description;
}
