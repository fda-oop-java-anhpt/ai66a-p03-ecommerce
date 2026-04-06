package com.oop.project.service.impl;

import com.oop.project.model.Order;
import com.oop.project.repository.OrderRepository;
import com.oop.project.service.interfaces.IDashboardService;

import java.sql.Timestamp;
import java.util.*;

public class DashboardServiceImpl implements IDashboardService {

    private final OrderRepository orderRepo;

    public DashboardServiceImpl(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    public List<Order> getAllOrders(String sortBy, boolean ascending) {
        List<Order> orders = orderRepo.findAll();
        Comparator<Order> comparator;
        switch (sortBy != null ? sortBy.toLowerCase() : "date") {
            case "customer":
                comparator = Comparator.comparing(
                        o -> o.getCustomer() != null ? o.getCustomer().getCustomerName() : "",
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case "amount":
                comparator = Comparator.comparing(Order::getFinalTotal);
                break;
            case "status":
                comparator = Comparator.comparing(o -> o.getStatus().name());
                break;
            case "date":
            default:
                comparator = Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }
        if (!ascending) comparator = comparator.reversed();
        orders.sort(comparator);
        return orders;
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
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalOrders", orderRepo.countAll());
        stats.put("totalRevenue", orderRepo.sumRevenue());
        stats.put("cancelledOrders", orderRepo.countByStatus("CANCELLED"));
        return stats;
    }
}