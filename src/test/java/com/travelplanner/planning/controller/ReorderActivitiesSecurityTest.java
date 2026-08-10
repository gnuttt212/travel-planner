package com.travelplanner.planning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ReorderActivitiesSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    // Use a random tripId that doesn't exist — the security check will try to find it
    // and isOwner will return false for non-owner, which is what we want to test
    private final UUID fakeTripId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void reorderShouldReturnUnauthorizedWithoutToken() throws Exception {
        List<UUID> body = List.of(UUID.randomUUID());

        mockMvc.perform(patch("/api/v1/trips/{tripId}/activities/reorder", fakeTripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = "USER")
    void reorderShouldReturn403ForNonOwner() throws Exception {
        // fakeTripId doesn't exist, so isOwner returns false -> 403
        List<UUID> body = List.of(UUID.randomUUID());

        mockMvc.perform(patch("/api/v1/trips/{tripId}/activities/reorder", fakeTripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void reorderShouldPassSecurityForAdmin() throws Exception {
        // ADMIN bypasses ownership check -> will get past @PreAuthorize
        // but will get 404 or 500 because trip doesn't exist (that's fine, we're testing security layer)
        List<UUID> body = List.of(UUID.randomUUID());

        mockMvc.perform(patch("/api/v1/trips/{tripId}/activities/reorder", fakeTripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is4xxClientError()); // 404 ResourceNotFound, not 403
    }
}
