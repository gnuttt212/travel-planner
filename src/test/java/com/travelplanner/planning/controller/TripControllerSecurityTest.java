package com.travelplanner.planning.controller;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.common.exception.GlobalExceptionHandler;
import com.travelplanner.planning.domain.Trip;
import com.travelplanner.planning.dto.TripResponse;
import com.travelplanner.planning.security.TripSecurity;
import com.travelplanner.planning.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test bảo mật cho TripController (kiểm tra các endpoint có check quyền đúng).
 * Sử dụng MockMvc standalone kết hợp với Mockito.
 */
@ExtendWith(MockitoExtension.class)
class TripControllerSecurityTest {

    private MockMvc mockMvc;

    @Mock
    private TripService tripService;

    @Mock
    private TripSecurity tripSecurity;

    @InjectMocks
    private TripController tripController;

    private static final UUID TRIP_ID = UUID.randomUUID();
    private static final String OWNER_EMAIL = "owner@test.com";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== GET /trips/{id} ====================

    @Test
    @DisplayName("getTrip - User có quyền xem -> 200 OK")
    void getTrip_hasPermission_returns200() throws Exception {
        when(tripSecurity.canView(eq(TRIP_ID), any())).thenReturn(true);
        Trip trip = new Trip();
        trip.setId(TRIP_ID);
        trip.setTitle("Test Trip");
        when(tripService.getById(TRIP_ID)).thenReturn(trip);

        mockMvc.perform(get("/api/v1/trips/" + TRIP_ID)
                .principal(new UsernamePasswordAuthenticationToken(OWNER_EMAIL, null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("getTrip - User không có quyền xem -> 403 Forbidden")
    void getTrip_noPermission_returns403() throws Exception {
        when(tripSecurity.canView(eq(TRIP_ID), any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/trips/" + TRIP_ID)
                .principal(new UsernamePasswordAuthenticationToken("stranger@test.com", null, List.of())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bạn không có quyền truy cập trip này."));
    }

    // ==================== GET /users/{email}/trips ====================

    @Test
    @DisplayName("getTripsByOwner - Trả về danh sách trip được filter bởi TripSecurity")
    void getTripsByOwner_returnsFilteredTrips() throws Exception {
        Trip trip = new Trip();
        trip.setId(TRIP_ID);
        trip.setTitle("Public Trip");
        
        when(tripSecurity.getVisibleTrips(eq(OWNER_EMAIL), eq("viewer@test.com"), any()))
                .thenReturn(List.of(trip));

        mockMvc.perform(get("/api/v1/users/" + OWNER_EMAIL + "/trips")
                .principal(new UsernamePasswordAuthenticationToken("viewer@test.com", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Public Trip"));
    }
}
