package com.travelplanner.budget.security;

import com.travelplanner.budget.domain.Budget;
import com.travelplanner.budget.repository.BudgetRepository;
import com.travelplanner.common.port.TripAccessPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetSecurityTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TripAccessPort tripAccessPort;

    @InjectMocks
    private BudgetSecurity budgetSecurity;

    private Authentication auth;
    private final String budgetId = "b1";
    private final String itineraryId = "trip1";
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        auth = mock(Authentication.class);
    }

    @Test
    void isOwner_Admin_ReturnsTrue() {
        Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(auth).getAuthorities();
        assertTrue(budgetSecurity.isOwner(budgetId, auth));
    }

    @Test
    void isOwner_ValidOwner_ReturnsTrue() {
        Budget budget = Budget.builder().itineraryId(itineraryId).build();
        Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();
        when(auth.getName()).thenReturn(userEmail);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(tripAccessPort.isOwner(itineraryId, userEmail)).thenReturn(true);

        assertTrue(budgetSecurity.isOwner(budgetId, auth));
    }

    @Test
    void isOwner_NotOwner_ReturnsFalse() {
        Budget budget = Budget.builder().itineraryId(itineraryId).build();
        Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();
        when(auth.getName()).thenReturn(userEmail);
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(tripAccessPort.isOwner(itineraryId, userEmail)).thenReturn(false);

        assertFalse(budgetSecurity.isOwner(budgetId, auth));
    }

    @Test
    void isOwner_NullItineraryId_ReturnsFalse() {
        Budget budget = Budget.builder().itineraryId(null).build();
        Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        assertFalse(budgetSecurity.isOwner(budgetId, auth));
    }

    @Test
    void isOwner_EmptyItineraryId_ReturnsFalse() {
        Budget budget = Budget.builder().itineraryId("   ").build();
        Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        assertFalse(budgetSecurity.isOwner(budgetId, auth));
    }

    @Test
    void isOwner_BudgetNotFound_ReturnsFalse() {
        Mockito.doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        assertFalse(budgetSecurity.isOwner(budgetId, auth));
    }
}
