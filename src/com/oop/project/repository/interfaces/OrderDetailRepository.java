package com.oop.project.repository.interfaces;

import java.util.List;

import com.oop.project.model.OrderDetail;

public interface OrderDetailRepository {
    List<OrderDetail> findByOrderId(int orderId);
    boolean insertBatch(int orderId, List<OrderDetail> details);
    boolean deleteByOrderId(int orderId);
}