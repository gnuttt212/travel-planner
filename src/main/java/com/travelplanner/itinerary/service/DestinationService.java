package com.travelplanner.itinerary.service;

import com.travelplanner.itinerary.dto.DestinationRequest;
import com.travelplanner.itinerary.dto.DestinationResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface duoc dat o package service (khong phai service.impl) de
 * Controller chi phu thuoc vao abstraction, khong phu thuoc vao chi tiet
 * implementation (Dependency Inversion Principle - chu D trong SOLID).
 * Sau nay neu doi cach xu ly (vi du them buoc goi vector search truoc
 * khi loc), ta chi sua DestinationServiceImpl ma khong dong den Controller.
 */
public interface DestinationService {

    DestinationResponse create(DestinationRequest request);

    DestinationResponse getById(String id);

    List<DestinationResponse> getAll();

    /**
     * Goi y diem den phu hop dua tren ngan sach/ngay va thang du dinh di.
     * Day la buoc "Loc theo rang buoc cung" trong pipeline goi y da thiet
     * ke - ban co the mo rong them buoc vector search phia truoc ham nay.
     */
    List<DestinationResponse> recommend(BigDecimal maxBudgetPerDay, Integer travelMonth);

    DestinationResponse update(String id, DestinationRequest request);

    void delete(String id);
}
