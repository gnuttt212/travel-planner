package com.travelplanner.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.budget.domain.Expense;
import com.travelplanner.budget.security.BudgetSecurity;
import com.travelplanner.budget.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class BudgetControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private BudgetSecurity budgetSecurity;

    private final String budgetId = "b1";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "USER")
    void getExpenses_Owner_Returns200() throws Exception {
        when(budgetSecurity.isOwner(eq(budgetId), any())).thenReturn(true);
        when(budgetService.getExpensesByBudget(budgetId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/budgets/{budgetId}/expenses", budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = "USER")
    void getExpenses_NotOwner_Returns403() throws Exception {
        when(budgetSecurity.isOwner(eq(budgetId), any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/budgets/{budgetId}/expenses", budgetId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "USER")
    void addExpense_Owner_Returns200() throws Exception {
        Expense expense = Expense.builder().amount(BigDecimal.TEN).build();
        when(budgetSecurity.isOwner(eq(budgetId), any())).thenReturn(true);
        when(budgetService.addExpense(any())).thenReturn(expense);

        mockMvc.perform(post("/api/v1/budgets/{budgetId}/expenses", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = "USER")
    void addExpense_NotOwner_Returns403() throws Exception {
        Expense expense = Expense.builder().amount(BigDecimal.TEN).build();
        when(budgetSecurity.isOwner(eq(budgetId), any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/budgets/{budgetId}/expenses", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getSummary_Admin_Returns200() throws Exception {
        when(budgetSecurity.isOwner(eq(budgetId), any())).thenReturn(true);
        when(budgetService.getBudgetSummary(budgetId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/budgets/{budgetId}/summary", budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
