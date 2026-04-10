package com.oop.project.service.impl;

import com.oop.project.model.*;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.OrderDetailRepository;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.service.interfaces.IOrderService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository orderDetailRepo;
    private final AuditLogRepository auditLogRepo;

    public OrderServiceImpl(OrderRepository orderRepo, OrderDetailRepository orderDetailRepo,
            AuditLogRepository auditLogRepo) {
        this.orderRepo = orderRepo;
        this.orderDetailRepo = orderDetailRepo;
        this.auditLogRepo = auditLogRepo;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    @Override
    public Optional<Order> getOrderById(int id) {
        return Optional.ofNullable(orderRepo.findById(id));
    }

    @Override
    public List<Order> searchOrders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllOrders();
        }
        return orderRepo.searchByCustomerNameOrId(keyword.trim());
    }

    @Override
    public List<Order> filterByStatus(OrderStatus status) {
        return orderRepo.filterByStatusOrDateRange(status.name(), null, null);
    }

    @Override
    public List<Order> filterByDateRange(Timestamp start, Timestamp end) {
        return orderRepo.filterByStatusOrDateRange(null, start, end);
    }

    @Override
    public void createOrder(Order order, List<OrderDetail> details, User currentUser) {
        if (order == null || details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Order and order details cannot be null or empty.");
        }
        order.setOrderItems(details);
        if (order.getOrderDate() == null) {
            order.setOrderDate(new Timestamp(System.currentTimeMillis()));
        }
        int orderId = orderRepo.insert(order);
        if (orderId > 0) {
            orderDetailRepo.insertBatch(orderId, details);
            logAudit(currentUser, "CREATE", "ORDER", String.valueOf(orderId));
        }
    }

    @Override
    public void updateOrderStatus(int id, OrderStatus status, User currentUser) {
        if (orderRepo.updateStatus(id, status.name())) {
            logAudit(currentUser, "UPDATE_STATUS", "ORDER", id + " to " + status);
        }
    }

    @Override
    public void cancelOrder(int id, User currentUser) {
        if (orderRepo.updateStatus(id, OrderStatus.CANCELLED.name())) {
            logAudit(currentUser, "CANCEL", "ORDER", String.valueOf(id));
        }
    }

    private void logAudit(User user, String action, String target, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setActions(action);
        log.setTargetType(target);
        log.setTargetId(details);
        log.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        auditLogRepo.insert(log);
    }
}