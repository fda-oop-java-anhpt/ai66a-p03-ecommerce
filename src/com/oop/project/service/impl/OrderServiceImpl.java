package com.oop.project.service.impl;

import com.oop.project.exception.ResourceNotFoundException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.*;
import com.oop.project.repository.interfaces.*;
import com.oop.project.repository.impl.*;
import com.oop.project.service.interfaces.IOrderService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository orderDetailRepo;
    private final AuditLogRepository auditLogRepo;

    public OrderServiceImpl() {
        this.orderRepo = new OrderRepositoryImpl();
        this.orderDetailRepo = new OrderDetailRepositoryImpl();
        this.auditLogRepo = new AuditLogRepositoryImpl();
    }

    public OrderServiceImpl(OrderRepository orderRepo, OrderDetailRepository orderDetailRepo, AuditLogRepository auditLogRepo) {
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
        Order order = orderRepo.findById(id);
        if (order != null) {
            // Load order details
            List<OrderDetail> details = orderDetailRepo.findByOrderId(id);
            order.setOrderItems(details);
        }
        return Optional.ofNullable(order);
    }

    @Override
    public List<Order> searchOrders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return orderRepo.findAll();
        }
        return orderRepo.searchByCustomerNameOrId(keyword.trim());
    }

    @Override
    public List<Order> filterByStatus(OrderStatus status) {
        if (status == null) return orderRepo.findAll();
        return orderRepo.filterByStatusOrDateRange(status.name(), null, null);
    }

    @Override
    public List<Order> filterByDateRange(Timestamp start, Timestamp end) {
        if (start == null || end == null) throw new ValidationException("Start and end dates must be provided.");
        return orderRepo.filterByStatusOrDateRange(null, start, end);
    }

    @Override
    public void createOrder(Order order, List<OrderDetail> details, User currentUser) {
        if (order == null) throw new ValidationException("Order cannot be null.");
        if (details == null || details.isEmpty()) throw new ValidationException("Order must have at least one item.");
        if (currentUser == null) throw new ValidationException("Current user cannot be null.");
        if (order.getCustomer() == null) throw new ValidationException("Order must have a customer.");

        // Basic validation: ensure each detail has item and quantity >0
        for (OrderDetail detail : details) {
            if (detail.getItem() == null) throw new ValidationException("Order detail missing item.");
            if (detail.getQuantity() <= 0) throw new ValidationException("Quantity must be positive.");
            if (detail.getPriceAtTime() == null || detail.getPriceAtTime().compareTo(java.math.BigDecimal.ZERO) <= 0)
                throw new ValidationException("Price at time must be positive.");
        }

        order.setOrderItems(details);
        order.setStatus(OrderStatus.PENDING);
        if (order.getOrderDate() == null) {
            order.setOrderDate(new Timestamp(System.currentTimeMillis()));
        }

        int orderId = orderRepo.insert(order);
        if (orderId <= 0) throw new RuntimeException("Failed to create order.");
        order.setOrderId(orderId);

        for (OrderDetail detail : details) {
            detail.setOrderId(orderId);
        }
        orderDetailRepo.insertBatch(orderId, details);

        // Audit log
        logAudit(currentUser, "CREATE_ORDER", "ORDER", String.valueOf(orderId));
    }

    @Override
    public void updateOrderStatus(int id, OrderStatus status, User currentUser) {
        if (currentUser == null) throw new ValidationException("Current user cannot be null.");
        Order existing = orderRepo.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + id);
        }
        boolean updated = orderRepo.updateStatus(id, status.name());
        if (!updated) throw new RuntimeException("Failed to update order status.");
        logAudit(currentUser, "UPDATE_STATUS_TO_" + status.name(), "ORDER", String.valueOf(id));
    }

    @Override
    public void cancelOrder(int id, User currentUser) {
        if (currentUser == null) throw new ValidationException("Current user cannot be null.");
        Order existing = orderRepo.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + id);
        }
        if (existing.getStatus() == OrderStatus.CANCELLED) {
            throw new ValidationException("Order is already cancelled.");
        }
        boolean updated = orderRepo.updateStatus(id, OrderStatus.CANCELLED.name());
        if (!updated) throw new RuntimeException("Failed to cancel order.");
        logAudit(currentUser, "CANCEL_ORDER", "ORDER", String.valueOf(id));
    }

    private void logAudit(User user, String action, String targetType, String targetId) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setActions(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        auditLogRepo.insert(log);
    }
}