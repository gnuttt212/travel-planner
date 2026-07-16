package com.travelplanner.booking.service;

import com.travelplanner.booking.domain.BookingProvider;
import com.travelplanner.booking.domain.BookingRequest;
import com.travelplanner.booking.domain.BookingResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * BookingService su dung Adapter Pattern.
 *
 * Spring tu dong inject tat ca cac bean implement BookingProvider vao danh sach.
 * Service nay xay dung mot Map tu providerType -> Provider, cho phep goi
 * dung provider dua tren loai dich vu (FLIGHT, HOTEL...) ma khong can
 * if-else hay switch-case.
 *
 * Khi can them nha cung cap moi (vi du: Car Rental), chi can tao class
 * moi implement BookingProvider, khong can sua BookingService (Open/Closed Principle).
 */
@Service
public class BookingService {

    private final Map<String, BookingProvider> providerMap;

    public BookingService(List<BookingProvider> providers) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(BookingProvider::getProviderType, Function.identity()));
    }

    public BookingResult book(BookingRequest request) {
        BookingProvider provider = providerMap.get(request.getProviderType());
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider type: " + request.getProviderType());
        }
        return provider.book(request);
    }

    public List<String> getAvailableProviders() {
        return List.copyOf(providerMap.keySet());
    }
}
