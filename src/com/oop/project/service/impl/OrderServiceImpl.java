package com.oop.project.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.Order;
import com.oop.project.repository.OrderRepository;
import com.oop.project.service.OrderService;

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(int orderId) {
        if (orderId <= 0) {
            System.out.println("Order ID must be greater than 0.");
            return Optional.empty();
        }
        return orderRepository.findById(orderId);
    }

    @Override
    public boolean createOrder(Order order) {
        if (!validateOrder(order)) return false;
        if (orderRepository.findById(order.getOrderId()).isPresent()) {
            System.out.println("Order already exists.");
            return false;
        }
        return orderRepository.save(order);
    }

    @Override
    public boolean updateOrder(Order order) {
        if (!validateOrder(order)) return false;
        if (orderRepository.findById(order.getOrderId()).isEmpty()) {
            System.out.println("Order not found.");
            return false;
        }
        return orderRepository.update(order);
    }

    @Override
    public boolean deleteOrder(int orderId) {
        if (orderId <= 0) {
            System.out.println("Order ID must be greater than 0.");
            return false;
        }
        if (orderRepository.findById(orderId).isEmpty()) {
            System.out.println("Order not found.");
            return false;
        }
        return orderRepository.deleteById(orderId);
    }

    private boolean validateOrder(Order order) {
        if (order == null) {
            System.out.println("Order cannot be null.");
            return false;
        }
        if (order.getOrderId() <= 0) {
            System.out.println("Order ID must be greater than 0.");
            return false;
        }
        if (order.getCustomer() == null) {
            System.out.println("Customer cannot be null.");
            return false;
        }
        if (order.getTaxRate() == null || order.getTaxRate().compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Tax rate cannot be negative.");
            return false;
        }
        if (order.getDiscountAmount() == null || order.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Discount amount cannot be negative.");
            return false;
        }
        if (order.getStatus() == null) {
            System.out.println("Order status cannot be null.");
            return false;
        }
        if (order.getSubtotal() == null || order.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Subtotal must be greater than 0.");
            return false;
        }
        if (order.getFinalTotal() == null || order.getFinalTotal().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Final total must be greater than 0.");
            return false;
        }
        return true;
    }
}