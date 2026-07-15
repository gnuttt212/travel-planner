package com.travelplanner.budget.controller;

import com.travelplanner.budget.domain.Expense;
import com.travelplanner.budget.dto.ExpenseSummaryDto;
import com.travelplanner.budget.service.BudgetService;
import com.travelplanner.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping("/{budgetId}/expenses")
    public ApiResponse<Expense> addExpense(@PathVariable String budgetId, @RequestBody Expense expense) {
        expense.setBudgetId(budgetId);
        return ApiResponse.success(budgetService.addExpense(expense));
    }

    @GetMapping("/{budgetId}/expenses")
    public ApiResponse<List<Expense>> getExpenses(@PathVariable String budgetId) {
        return ApiResponse.success(budgetService.getExpensesByBudget(budgetId));
    }

    @GetMapping("/{budgetId}/summary")
    public ApiResponse<List<ExpenseSummaryDto>> getSummary(@PathVariable String budgetId) {
        return ApiResponse.success(budgetService.getBudgetSummary(budgetId));
    }
}
