package com.travelplanner.planning.service;

import com.travelplanner.common.exception.ResourceNotFoundException;
import com.travelplanner.common.dto.RecommendationRequest;
import com.travelplanner.common.dto.RecommendationVariantResponse;
import com.travelplanner.planning.domain.*;
import com.travelplanner.planning.dto.TripRequest;
import com.travelplanner.planning.port.RecommendationPort;
import com.travelplanner.planning.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {
    
    private final TripRepository tripRepository;
    private final RecommendationPort recommendationPort;
    private final TripActivityMapper activityMapper;
    private final com.travelplanner.recommendation.integration.NominatimGeocodingClient geocodingClient;
    
    @Transactional
    public Trip createFromPlan(String userId, TripRequest request) {
        RecommendationRequest recRequest = RecommendationRequest.builder()
                .city(request.city())
                .startLat(request.startLat())
                .startLon(request.startLon())
                .transportationType(request.transportation())
                .date(request.tripDate())
                .styles(request.styles())
                .budgetPerPerson(request.budgetPerPerson())
                .durationType(request.duration())
                .build();
        
        List<RecommendationVariantResponse> variants = recommendationPort.recommend(recRequest);
        RecommendationVariantResponse selectedVariant = null;
        if (request.variantName() != null && !request.variantName().isBlank()) {
            selectedVariant = variants.stream()
                    .filter(v -> request.variantName().equals(v.getVariantName()))
                    .findFirst()
                    .orElse(null);
        }
        if (selectedVariant == null && !variants.isEmpty()) {
            selectedVariant = variants.get(0);
        }
        List<TripActivity> activities = selectedVariant == null ? List.of() : selectedVariant.getActivities().stream()
                .map(activityMapper::toTripActivity)
                .collect(Collectors.toList());
        
        Trip trip = Trip.builder()
            .ownerId(userId)
            .title("Chuyến đi " + request.city())
            .status(TripStatus.PLANNED)
            .tripDate(request.tripDate())
            .duration(TripDuration.valueOf(request.duration()))
            .purpose(TripPurpose.valueOf(request.purpose()))
            .groupSize(request.groupSize())
            .startLat(request.startLat())
            .startLon(request.startLon())
            .transportation(Transportation.fromApiValue(request.transportation()))
            .activities(new ArrayList<>(activities))
            .createdAt(Instant.now())
            .build();

        trip.getActivities().forEach(activity -> activity.setTrip(trip));
        
        return tripRepository.save(trip);
    }
    
    public List<Trip> getByOwner(String ownerId) {
        return tripRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }
    
    public Trip getById(UUID id) {
        return tripRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.of("Trip", id.toString()));
    }
    
    @Transactional
    public void delete(UUID id) {
        tripRepository.deleteById(id);
    }

    /**
     * Đổi thứ tự activities trong trip và tính lại thời gian/khoảng cách dây chuyền.
     * <p>
     * Validation: activityIdsInNewOrder phải chứa ĐÚNG và ĐỦ tập activityId hiện có.
     * Nếu sai → ném IllegalArgumentException, KHÔNG áp dụng thay đổi một phần.
     */
    @Transactional
    public Trip reorderActivities(UUID tripId, List<UUID> activityIdsInNewOrder) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> ResourceNotFoundException.of("Trip", tripId.toString()));

        List<TripActivity> currentActivities = trip.getActivities();

        // --- Validate: đúng và đủ ---
        Set<UUID> currentIds = currentActivities.stream()
                .map(TripActivity::getId)
                .collect(Collectors.toSet());
        Set<UUID> newIds = new LinkedHashSet<>(activityIdsInNewOrder);

        if (newIds.size() != activityIdsInNewOrder.size()) {
            throw new IllegalArgumentException(
                    "Danh sách activityId chứa phần tử trùng lặp");
        }
        if (!currentIds.equals(newIds)) {
            throw new IllegalArgumentException(
                    "Danh sách activityId không khớp với các activity hiện có của trip. "
                    + "Hiện có: " + currentIds + ", nhận được: " + newIds);
        }

        // --- Sắp xếp lại theo thứ tự mới ---
        Map<UUID, TripActivity> activityMap = currentActivities.stream()
                .collect(Collectors.toMap(TripActivity::getId, Function.identity()));

        // Lấy plannedStartTime gốc của activity đang ở vị trí 0 cũ (giờ bắt đầu chuyến đi)
        LocalTime tripStartTime = currentActivities.stream()
                .filter(a -> a.getOrderIndex() == 0)
                .map(TripActivity::getPlannedStartTime)
                .findFirst()
                .orElse(currentActivities.get(0).getPlannedStartTime());

        LocalTime cursor = tripStartTime;

        for (int i = 0; i < activityIdsInNewOrder.size(); i++) {
            TripActivity activity = activityMap.get(activityIdsInNewOrder.get(i));
            activity.setOrderIndex(i);

            if (i == 0) {
                // Activity đầu tiên: không có travel, giữ nguyên giờ bắt đầu chuyến đi
                activity.setTravelDistanceKm(0);
                activity.setTravelTimeMinutes(0);
                activity.setPlannedStartTime(cursor);
            } else {
                TripActivity prev = activityMap.get(activityIdsInNewOrder.get(i - 1));
                double distKm = haversineKm(
                        prev.getDestinationLat(), prev.getDestinationLon(),
                        activity.getDestinationLat(), activity.getDestinationLon());
                int travelMin = estimateTravelMinutes(distKm, trip.getTransportation());

                activity.setTravelDistanceKm(distKm);
                activity.setTravelTimeMinutes(travelMin);
                activity.setPlannedStartTime(cursor.plusMinutes(travelMin));
            }

            activity.setPlannedEndTime(
                    activity.getPlannedStartTime().plusMinutes(activity.getEstimatedDurationMinutes()));
            cursor = activity.getPlannedEndTime();
        }

        trip.setUpdatedAt(Instant.now());
        return tripRepository.save(trip);
    }

    // ---- Haversine fallback ----
    // TODO: Có thể đồng bộ lại route chính xác qua ORS bất đồng bộ sau,
    //       hiện dùng Haversine cho nhanh và tiết kiệm quota API.

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private int estimateTravelMinutes(double km, Transportation transport) {
        double speedKmh = switch (transport) {
            case MOTORBIKE -> 25;
            case CAR -> 30;
            case PUBLIC_TRANSPORT -> 15;
        };
        return Math.max(5, (int) Math.ceil((km / speedKmh) * 60));
    }
}
