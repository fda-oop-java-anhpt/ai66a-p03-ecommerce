package com.oop.project.service.interfaces;

import com.oop.project.model.Order;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface IDashboardService {
    List<Order> getAllOrders(String sortBy, boolean ascending);
    List<Order> filterOrders(String status, Timestamp from, Timestamp to);
    List<Order> searchOrders(String keyword);
    Map<String, Object> getSummaryStatistics();
}