package com.oop.project.service.interfaces;

import com.oop.project.model.Order;
import com.oop.project.model.OrderDetail;
import com.oop.project.model.OrderStatus;
import com.oop.project.model.User;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface IOrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(int id);
    List<Order> searchOrders(String keyword);
    List<Order> filterByStatus(OrderStatus status);
    List<Order> filterByDateRange(Timestamp start, Timestamp end);
    void createOrder(Order order, List<OrderDetail> details, User currentUser);
    void updateOrderStatus(int id, OrderStatus status, User currentUser);
    void cancelOrder(int id, User currentUser);
}