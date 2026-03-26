package com.oop.project.service;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Order;

public interface OrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(int orderId);
    boolean createOrder(Order order);
    boolean updateOrder(Order order);
    boolean deleteOrder(int orderId);
}