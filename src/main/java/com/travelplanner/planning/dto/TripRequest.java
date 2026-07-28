package com.travelplanner.planning.dto;

import com.travelplanner.planning.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TripRequest(
        String purpose,
        LocalDate tripDate,
        String duration,
        String groupType,
        int groupSize,
        BigDecimal budgetPerPerson,
        List<String> styles,
        double startLat,
        double startLon,
        String transportation,
        String city) {
    public TravelContext toTravelContext() {
        try {
            return new TravelContext(
                    TripPurpose.valueOf(purpose),
                    tripDate,
                    TripDuration.valueOf(duration),
                    groupType,
                    groupSize,
                    budgetPerPerson,
                    styles,
                    startLat,
                    startLon,
                    Transportation.valueOf(transportation));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Giá trị không hợp lệ: " + e.getMessage());
        }
    }
}
