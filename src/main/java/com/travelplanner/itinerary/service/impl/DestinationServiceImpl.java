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

import com.travelplanner.user.domain.UserPreference;
import com.travelplanner.user.repository.UserPreferenceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // constructor injection - de test, tuan thu DIP
@Transactional(readOnly = true)
public class DestinationServiceImpl implements DestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;
    private final UserPreferenceRepository userPreferenceRepository;

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
    public List<DestinationResponse> recommend(String userId, BigDecimal maxBudgetPerDay, Integer travelMonth) {
        Optional<UserPreference> prefOpt = userPreferenceRepository.findByUserId(userId);
        
        if (prefOpt.isPresent() && prefOpt.get().getPreferenceVector() != null) {
            String vectorStr = prefOpt.get().getPreferenceVector();
            // C = 10 (min reviews), m = 4.0 (avg rating) for Bayesian calc
            List<Destination> recommended = destinationRepository.findRecommendedDestinationsNative(
                    maxBudgetPerDay, travelMonth, vectorStr, 10.0, 4.0, 5);
            
            return recommended.stream()
                    .map(dest -> {
                        DestinationResponse response = destinationMapper.toResponse(dest);
                        String reason = generateRecommendationReason(dest, prefOpt.get());
                        return new DestinationResponse(
                                response.id(), response.name(), response.country(), response.description(),
                                response.tags(), response.bestMonths(), response.avgCostPerDay(),
                                response.popularityScore(), response.latitude(), response.longitude(),
                                reason
                        );
                    })
                    .toList();
        }
        
        // Fallback to basic hard filter
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

    private String generateRecommendationReason(Destination dest, UserPreference pref) {
        if (dest.getTags() == null || pref.getTagWeights() == null) {
            return "Được đề xuất qua thuật toán AI phân tích tương đồng sở thích của bạn.";
        }
        
        List<String> matchingTags = dest.getTags().stream()
                .filter(tag -> pref.getTagWeights().containsKey(tag))
                .toList();
                
        if (!matchingTags.isEmpty()) {
            return "Được đề xuất vì bạn thích các yếu tố: " + String.join(", ", matchingTags) + ".";
        }
        return "Được đề xuất qua thuật toán AI phân tích tương đồng sở thích của bạn.";
    }
}
