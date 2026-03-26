package com.oop.project.service;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.OrderDetail;

public interface OrderDetailService {
    List<OrderDetail> getAllOrderDetails();
    List<OrderDetail> getOrderDetailsByOrderId(int orderId);
    Optional<OrderDetail> getOrderDetailById(int orderDetailId);
    boolean createOrderDetail(OrderDetail detail);
    boolean updateOrderDetail(OrderDetail detail);
    boolean deleteOrderDetail(int orderDetailId);
}