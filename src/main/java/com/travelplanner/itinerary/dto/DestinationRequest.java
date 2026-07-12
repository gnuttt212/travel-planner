package com.travelplanner.itinerary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tach biet voi Entity (khong dung entity truc tiep lam request body)
 * de: (1) kiem soat duoc field nao client duoc phep gui, (2) validate
 * doc lap voi rang buoc database, (3) tranh loi over-posting.
 */
public record DestinationRequest(

        @NotBlank(message = "Ten diem den khong duoc de trong")
        String name,

        String country,

        String description,

        List<String> tags,

        List<Integer> bestMonths,

        @NotNull(message = "Chi phi trung binh/ngay khong duoc de trong")
        @PositiveOrZero(message = "Chi phi phai >= 0")
        BigDecimal avgCostPerDay
) {
}
