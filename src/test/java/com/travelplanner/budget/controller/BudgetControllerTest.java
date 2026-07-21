package com.travelplanner.budget.controller;

import com.travelplanner.budget.domain.Expense;
import com.travelplanner.budget.dto.ExpenseSummaryDto;
import com.travelplanner.budget.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetService budgetService;

    @Test
    void summaryShouldReturnBudgetSummary() throws Exception {
        ExpenseSummaryDto summary = new ExpenseSummaryDto("Flight", 2, 500000.0);
        when(budgetService.getBudgetSummary("budget-1")).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/budgets/budget-1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("Flight"));
    }
}
