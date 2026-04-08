package com.oop.project.service.impl;

import com.oop.project.model.*;
import com.oop.project.repository.*;
import com.oop.project.repository.impl.*;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.CustomerRepository;
import com.oop.project.repository.interfaces.OrderDetailRepository;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.service.interfaces.BillingService;
import com.oop.project.service.interfaces.CouponService;
import com.oop.project.service.interfaces.OrderService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService.
 *
 * FR-3: Order and Billing Management
 * FR-4.3: Order status categories (Pending, Paid, Cancelled)
 * FR-4.4: Audit log for order creation, updates, and cancellations
 * FR-5: Order Dashboard and Search
 *
 * @author Lan - Service Layer
 */
public class OrderServiceImpl implements OrderService {

    // ── Dependencies ──────────────────────────────────────────────
    private final OrderRepository       orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository    customerRepository;
    private final AuditLogRepository    auditLogRepository;
    private final BillingService        billingService;
    
    // ── Constructor ───────────────────────────────────────────────
    public OrderServiceImpl() {
        this.orderRepository       = new OrderRepositoryImpl();
        this.orderDetailRepository = new OrderDetailRepositoryImpl();
        this.customerRepository    = new CustomerRepositoryImpl();
        this.auditLogRepository    = new AuditLogRepositoryImpl();
        this.billingService        = new BillingServiceImpl();
    }

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderDetailRepository orderDetailRepository,
                            CustomerRepository customerRepository,
                            AuditLogRepository auditLogRepository,
                            BillingService billingService,
                            CouponService couponService) {
        this.orderRepository       = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.customerRepository    = customerRepository;
        this.auditLogRepository    = auditLogRepository;
        this.billingService        = billingService;
    }

    // ─────────────────────────────────────────────────────────────
    // READ — FR-5.1
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(int orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> getOrdersByCustomerId(int customerId) {
        return orderRepository.findAll().stream()
            .filter(o -> o.getCustomer() != null &&
                         o.getCustomer().getCustomerId() == customerId)
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE — FR-3.1 + FR-3.2 + FR-4.4
    // ─────────────────────────────────────────────────────────────

    /**
     * Create a new order with billing calculation.
     *
     * Steps:
     *  1. Validate customer exists
     *  2. Validate orderDetails list is not empty
     *  3. Apply coupon if attached to the order
     *  4. Calculate billing (via BillingService)
     *  5. Set initial status to PENDING
     *  6. Save Order to DB → get orderId
     *  7. Save each OrderDetail with the new orderId
     *  8. Record "CREATE_ORDER" in AuditLog (FR-4.4)
     */
    @Override
    public Order createOrder(Order order, List<OrderDetail> orderDetails, User actor) {
        // Step 1: Validate customer
        if (order.getCustomer() == null) {
            throw new IllegalArgumentException("Order must have a customer.");
        }
        customerRepository.findById(order.getCustomer().getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Customer not found: ID " + order.getCustomer().getCustomerId()));

        // Step 2: Validate items
        if (orderDetails == null || orderDetails.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        // Step 3: Attach orderDetails to the Order object
        order.setOrderItems(orderDetails);

        // Step 4: Calculate billing (calls BillingService.calculateOrderBilling)
        billingService.calculateOrderBilling(order);

        // Step 5: Set default status and timestamp
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(Timestamp.from(Instant.now()));

        // Step 6: Save order to DB
        boolean saved = orderRepository.save(order);
        if (!saved) return null;

        // Step 7: Save each OrderDetail (link orderId)
        for (OrderDetail detail : orderDetails) {
            detail.setOrderId(order.getOrderId());
            orderDetailRepository.save(detail);
        }

        // Step 8: Audit log (FR-4.4)
        recordAuditLog(actor, "CREATE_ORDER", "ORDER", String.valueOf(order.getOrderId()));

        return order;
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE STATUS — FR-3.1 + FR-4.3 + FR-4.4
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean updateOrderStatus(int orderId, OrderStatus newStatus, User actor) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Order not found: ID " + orderId));

        order.setStatus(newStatus);
        boolean result = orderRepository.update(order);

        if (result) {
            // Audit log (FR-4.4)
            recordAuditLog(actor, "UPDATE_STATUS → " + newStatus.name(), "ORDER",
                String.valueOf(orderId));
        }

        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // CANCEL — FR-3.1 + FR-4.4
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean cancelOrder(int orderId, User actor) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Order not found: ID " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order is already cancelled: ID " + orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        boolean result = orderRepository.update(order);

        if (result) {
            // Audit log cancellation (FR-4.4)
            recordAuditLog(actor, "CANCEL_ORDER", "ORDER", String.valueOf(orderId));
        }

        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // SEARCH + FILTER — FR-5.2 + FR-5.3
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Order> searchOrders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllOrders();

        String lower = keyword.trim().toLowerCase();
        return orderRepository.findAll().stream()
            .filter(o ->
                String.valueOf(o.getOrderId()).contains(keyword.trim()) ||
                (o.getCustomer() != null &&
                 o.getCustomer().getCustomerName().toLowerCase().contains(lower))
            )
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> filterByStatus(OrderStatus status) {
        return orderRepository.findAll().stream()
            .filter(o -> o.getStatus() == status)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> filterByDateRange(Timestamp from, Timestamp to) {
        return orderRepository.findAll().stream()
            .filter(o -> o.getOrderDate() != null &&
                         !o.getOrderDate().before(from) &&
                         !o.getOrderDate().after(to))
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────────────────────────

    private void recordAuditLog(User actor, String action, String targetType, String targetId) {
        if (actor == null) return;
        AuditLog log = new AuditLog(
            0, actor, action, targetType, targetId,
            Timestamp.from(Instant.now())
        );
        auditLogRepository.save(log);
    }
}