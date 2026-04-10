package com.oop.project.repository.interfaces;

import com.oop.project.model.OrderDetail;

import java.util.List;

public interface OrderDetailRepository {
    List<OrderDetail> findByOrderId(int orderId);

    boolean insertBatch(int orderId, List<OrderDetail> details);

    boolean deleteByOrderId(int orderId);
}