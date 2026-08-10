package com.travelplanner.budget.security;

import com.travelplanner.budget.repository.BudgetRepository;
import com.travelplanner.common.port.TripAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("budgetSecurity")
@RequiredArgsConstructor
public class BudgetSecurity {

    private final BudgetRepository budgetRepository;
    private final TripAccessPort tripAccessPort;

    public boolean isOwner(String budgetId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        return budgetRepository.findById(budgetId)
                .map(budget -> {
                    String itineraryId = budget.getItineraryId();
                    if (itineraryId == null || itineraryId.trim().isEmpty()) {
                        return false;
                    }
                    return tripAccessPort.isOwner(itineraryId, authentication.getName());
                })
                .orElse(false);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
