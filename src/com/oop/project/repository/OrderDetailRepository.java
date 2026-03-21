package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.OrderDetail;

public interface OrderDetailRepository {
    List<OrderDetail> findAll();
    List<OrderDetail> findByOrderId(int orderId);
    Optional<OrderDetail> findById(int orderDetailId);
    boolean save(OrderDetail detail);
    boolean update(OrderDetail detail);
    boolean deleteById(int orderDetailId);
}