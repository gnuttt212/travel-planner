package com.travelplanner.budget.repository;

import com.travelplanner.budget.domain.Expense;
import com.travelplanner.budget.dto.ExpenseSummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface ExpenseRepository extends JpaRepository<Expense, String> {

    List<Expense> findByBudgetId(String budgetId);

    @Query("""
            SELECT new com.travelplanner.budget.dto.ExpenseSummaryDto(
                e.category, 
                CAST(e.expenseDate AS string), 
                SUM(e.amount)
            )
            FROM Expense e 
            WHERE e.budgetId = :budgetId 
            GROUP BY e.category, e.expenseDate
            """)
    List<ExpenseSummaryDto> getSummaryByBudgetId(@Param("budgetId") String budgetId);
}
