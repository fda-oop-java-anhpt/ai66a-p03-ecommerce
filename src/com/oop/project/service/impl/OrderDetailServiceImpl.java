package com.oop.project.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.OrderDetail;
import com.oop.project.repository.OrderDetailRepository;
import com.oop.project.service.OrderDetailService;

public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;

    public OrderDetailServiceImpl(OrderDetailRepository orderDetailRepository) {
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    public List<OrderDetail> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    @Override
    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) {
        if (orderId <= 0) {
            System.out.println("Order ID must be greater than 0.");
            return List.of();
        }
        return orderDetailRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<OrderDetail> getOrderDetailById(int orderDetailId) {
        if (orderDetailId <= 0) {
            System.out.println("Order detail ID must be greater than 0.");
            return Optional.empty();
        }
        return orderDetailRepository.findById(orderDetailId);
    }

    @Override
    public boolean createOrderDetail(OrderDetail detail) {
        if (!validateOrderDetail(detail)) return false;
        if (orderDetailRepository.findById(detail.getOrderDetailId()).isPresent()) {
            System.out.println("Order detail already exists.");
            return false;
        }
        return orderDetailRepository.save(detail);
    }

    @Override
    public boolean updateOrderDetail(OrderDetail detail) {
        if (!validateOrderDetail(detail)) return false;
        if (orderDetailRepository.findById(detail.getOrderDetailId()).isEmpty()) {
            System.out.println("Order detail not found.");
            return false;
        }
        return orderDetailRepository.update(detail);
    }

    @Override
    public boolean deleteOrderDetail(int orderDetailId) {
        if (orderDetailId <= 0) {
            System.out.println("Order detail ID must be greater than 0.");
            return false;
        }
        if (orderDetailRepository.findById(orderDetailId).isEmpty()) {
            System.out.println("Order detail not found.");
            return false;
        }
        return orderDetailRepository.deleteById(orderDetailId);
    }

    private boolean validateOrderDetail(OrderDetail detail) {
        if (detail == null) {
            System.out.println("Order detail cannot be null.");
            return false;
        }
        if (detail.getOrderDetailId() <= 0) {
            System.out.println("Order detail ID must be greater than 0.");
            return false;
        }
        if (detail.getOrderId() <= 0) {
            System.out.println("Order ID must be greater than 0.");
            return false;
        }
        if (detail.getItem() == null) {
            System.out.println("Item cannot be null.");
            return false;
        }
        if (detail.getQuantity() <= 0) {
            System.out.println("Quantity must be greater than 0.");
            return false;
        }
        BigDecimal price = detail.getPriceAtTime();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Price at time must be greater than 0.");
            return false;
        }
        return true;
    }
}