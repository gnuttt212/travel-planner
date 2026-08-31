-- V3: Remove unused budget module tables
-- These tables were part of a budget/expense tracking feature that was never
-- integrated with the frontend. No FK constraints reference them.

DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS budgets;
