package com.oop.project.service.interfaces;

import java.math.BigDecimal;

/**
 * FR-5.4: Dashboard Summary Statistics
 *
 * Provides aggregated summary data for the Dashboard tab (Tab 5).
 * All statistics are computed from existing Order data.
 *
 * Statistics displayed in the Dashboard:
 *   - Total number of orders
 *   - Total revenue (sum of finalTotal for PAID orders)
 *   - Number of cancelled orders
 *
 * @author Lan - Service Layer
 */
public interface DashboardService {

    /**
     * Get the total number of orders in the system.
     *
     * FR-5.4: Total orders count for the summary card.
     *
     * @return the total order count (all statuses)
     */
    int getTotalOrderCount();

    /**
     * Get the total revenue from all PAID orders.
     *
     * FR-5.4: Total revenue for the summary card.
     * Only counts orders with status = PAID.
     *
     * @return the sum of finalTotal for all PAID orders
     */
    BigDecimal getTotalRevenue();

    /**
     * Get the number of orders with status = CANCELLED.
     *
     * FR-5.4: Cancelled orders count for the summary card.
     *
     * @return the count of cancelled orders
     */
    int getCancelledOrderCount();

    /**
     * Get the number of orders with status = PENDING.
     *
     * @return the count of pending orders
     */
    int getPendingOrderCount();
}
