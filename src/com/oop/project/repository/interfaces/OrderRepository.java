package com.oop.project.repository;

import com.oop.project.model.Order;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public interface OrderRepository {
    List<Order> findAll();
    Order findById(int id);
    List<Order> findByCustomerId(int customerId);
    List<Order> searchByCustomerNameOrId(String keyword);
    List<Order> filterByStatusOrDateRange(String status, Timestamp from, Timestamp to);
    int insert(Order order);
    boolean update(Order order);
    boolean updateStatus(int orderId, String status);
    boolean delete(int orderId);
    int countAll();
    int countByStatus(String status);
    BigDecimal sumRevenue();
}