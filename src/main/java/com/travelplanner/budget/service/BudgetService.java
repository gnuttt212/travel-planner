package com.travelplanner.budget.service;

import com.travelplanner.budget.domain.Expense;
import com.travelplanner.budget.dto.ExpenseSummaryDto;
import com.travelplanner.budget.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final ExpenseRepository expenseRepository;

    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByBudget(String budgetId) {
        return expenseRepository.findByBudgetId(budgetId);
    }

    public List<ExpenseSummaryDto> getBudgetSummary(String budgetId) {
        return expenseRepository.getSummaryByBudgetId(budgetId);
    }
}
