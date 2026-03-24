package com.oop.project.service.impl;

import com.oop.project.model.Order;
import com.oop.project.model.OrderStatus;
import com.oop.project.repository.OrderRepository;
import com.oop.project.repository.impl.OrderRepositoryImpl;
import com.oop.project.service.interfaces.DashboardService;

import java.math.BigDecimal;

/**
 * Implementation of DashboardService.
 *
 * FR-5.4: Dashboard summary statistics
 *   - Total orders count
 *   - Total revenue (PAID orders)
 *   - Cancelled orders count
 *   - Pending orders count
 *
 * @author Lan - Service Layer
 */
public class DashboardServiceImpl implements DashboardService {

    // ── Dependencies ──────────────────────────────────────────────
    private final OrderRepository orderRepository;

    // ── Constructor ───────────────────────────────────────────────
    public DashboardServiceImpl() {
        this.orderRepository = new OrderRepositoryImpl();
    }

    public DashboardServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // FR-5.4: SUMMARY STATISTICS
    // ─────────────────────────────────────────────────────────────

    @Override
    public int getTotalOrderCount() {
        return orderRepository.findAll().size();
    }

    /**
     * Total revenue = sum of finalTotal for all PAID orders only.
     */
    @Override
    public BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
            .filter(o -> o.getStatus() == OrderStatus.PAID && o.getFinalTotal() != null)
            .map(Order::getFinalTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getCancelledOrderCount() {
        return (int) orderRepository.findAll().stream()
            .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
            .count();
    }

    @Override
    public int getPendingOrderCount() {
        return (int) orderRepository.findAll().stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING)
            .count();
    }
}