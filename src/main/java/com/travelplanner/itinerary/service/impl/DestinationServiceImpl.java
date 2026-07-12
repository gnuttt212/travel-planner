package com.travelplanner.itinerary.service.impl;

import com.travelplanner.common.exception.ResourceNotFoundException;
import com.travelplanner.itinerary.domain.Destination;
import com.travelplanner.itinerary.dto.DestinationRequest;
import com.travelplanner.itinerary.dto.DestinationResponse;
import com.travelplanner.itinerary.mapper.DestinationMapper;
import com.travelplanner.itinerary.repository.DestinationRepository;
import com.travelplanner.itinerary.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor // constructor injection - de test, tuan thu DIP
@Transactional(readOnly = true)
public class DestinationServiceImpl implements DestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;

    @Override
    @Transactional
    public DestinationResponse create(DestinationRequest request) {
        Destination saved = destinationRepository.save(destinationMapper.toEntity(request));
        return destinationMapper.toResponse(saved);
    }

    @Override
    public DestinationResponse getById(String id) {
        Destination destination = findEntityOrThrow(id);
        return destinationMapper.toResponse(destination);
    }

    @Override
    public List<DestinationResponse> getAll() {
        return destinationRepository.findAll().stream()
                .map(destinationMapper::toResponse)
                .toList();
    }

    @Override
    public List<DestinationResponse> recommend(BigDecimal maxBudgetPerDay, Integer travelMonth) {
        // Buoc "Loc theo rang buoc cung" trong pipeline goi y diem den.
        // TODO giai doan sau: them buoc vector search (tim kiem ngu nghia
        // theo so thich nguoi dung) truoc khi goi ham nay, roi ket hop
        // diem tuong dong voi popularityScore de xep hang cuoi cung.
        return destinationRepository.findMatchingDestinations(maxBudgetPerDay, travelMonth).stream()
                .map(destinationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DestinationResponse update(String id, DestinationRequest request) {
        Destination existing = findEntityOrThrow(id);
        existing.setName(request.name());
        existing.setCountry(request.country());
        existing.setDescription(request.description());
        existing.setTags(request.tags());
        existing.setBestMonths(request.bestMonths());
        existing.setAvgCostPerDay(request.avgCostPerDay());
        return destinationMapper.toResponse(existing);
        // Khong can goi save() rieng: entity dang o trang thai managed
        // trong persistence context nen JPA tu dong flush thay doi (dirty checking).
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!destinationRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Destination", id);
        }
        destinationRepository.deleteById(id);
    }

    private Destination findEntityOrThrow(String id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Destination", id));
    }
}
