/**
 * Module Ngan sach (Budget).
 *
 * Theo cung cau truc voi module itinerary: domain / dto / repository /
 * service / controller / mapper.
 *
 * Goi y entity ban dau:
 *   - Budget (tripId, totalAmount, currency)
 *   - Expense (budgetId, category, amount, note, spentAt)
 *
 * Goi y endpoint:
 *   POST   /api/v1/budgets
 *   POST   /api/v1/budgets/{id}/expenses
 *   GET    /api/v1/budgets/{id}/summary   -> tong hop chi phi theo category/ngay
 */
package com.travelplanner.budget;
