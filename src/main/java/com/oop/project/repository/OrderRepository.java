package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Order;

public interface OrderRepository {
    List<Order> findAll();
    Optional<Order> findById(int id);
    boolean save(Order order);
    boolean update(Order order);
    boolean deleteById(int id);
}