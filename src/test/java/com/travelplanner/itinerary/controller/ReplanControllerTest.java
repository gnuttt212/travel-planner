package com.travelplanner.itinerary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.common.security.JwtAuthenticationFilter;
import com.travelplanner.common.security.JwtUtil;
import com.travelplanner.itinerary.dto.DelayReportRequest;
import com.travelplanner.itinerary.service.AdaptiveReplanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReplanController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReplanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdaptiveReplanService adaptiveReplanService;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void reportDelay_ShouldReturnSuccess() throws Exception {
        UUID tripId = UUID.randomUUID();
        DelayReportRequest request = new DelayReportRequest(30, "Traffic");

        mockMvc.perform(post("/api/v1/itineraries/{tripId}/report-delay", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đang tạo đề xuất điều chỉnh..."));

        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @WithMockUser
    void resolveSuggestion_ShouldReturnSuccess() throws Exception {
        UUID suggestionId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/itineraries/suggestions/{suggestionId}/resolve", suggestionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã phản hồi đề xuất điều chỉnh"));

        verify(adaptiveReplanService).resolveSuggestion(suggestionId, true);
    }
}
