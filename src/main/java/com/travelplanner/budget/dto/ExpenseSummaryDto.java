package com.travelplanner.budget.dto;

import java.math.BigDecimal;

public record ExpenseSummaryDto(
        String category,
        String expenseDate,
        BigDecimal totalAmount
) {}
