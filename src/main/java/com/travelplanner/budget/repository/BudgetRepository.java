package com.travelplanner.budget.repository;

import com.travelplanner.budget.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface BudgetRepository extends JpaRepository<Budget, String> {
    List<Budget> findByItineraryId(String itineraryId);
}
