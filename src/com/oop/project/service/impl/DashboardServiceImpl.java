package com.oop.project.service.impl;

import com.oop.project.model.Order;
import com.oop.project.model.OrderStatus;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.repository.impl.OrderRepositoryImpl;
import com.oop.project.service.interfaces.IDashboardService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardServiceImpl implements IDashboardService {

    private final OrderRepository orderRepo;

    public DashboardServiceImpl() {
        this.orderRepo = new OrderRepositoryImpl();
    }

    public DashboardServiceImpl(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    public List<Order> getAllOrders(String sortBy, boolean ascending) {
        List<Order> orders = orderRepo.findAll();
        if (sortBy == null) return orders;
        Comparator<Order> comparator;
        switch (sortBy.toLowerCase()) {
            case "orderid":
                comparator = Comparator.comparingInt(Order::getOrderId);
                break;
            case "finaltotal":
                comparator = Comparator.comparing(Order::getFinalTotal, Comparator.nullsLast(BigDecimal::compareTo));
                break;
            case "orderdate":
                comparator = Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Timestamp::compareTo));
                break;
            default:
                return orders;
        }
        if (!ascending) comparator = comparator.reversed();
        return orders.stream().sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public List<Order> filterOrders(String status, Timestamp from, Timestamp to) {
        return orderRepo.filterByStatusOrDateRange(status, from, to);
    }

    @Override
    public List<Order> searchOrders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return orderRepo.findAll();
        }
        return orderRepo.searchByCustomerNameOrId(keyword.trim());
    }

    @Override
    public Map<String, Object> getSummaryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        int totalOrders = orderRepo.countAll();
        int pendingOrders = orderRepo.countByStatus(OrderStatus.PENDING.name());
        int paidOrders = orderRepo.countByStatus(OrderStatus.PAID.name());
        int cancelledOrders = orderRepo.countByStatus(OrderStatus.CANCELLED.name());
        BigDecimal totalRevenue = orderRepo.sumRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("paidOrders", paidOrders);
        stats.put("cancelledOrders", cancelledOrders);
        stats.put("totalRevenue", totalRevenue);
        return stats;
    }
}