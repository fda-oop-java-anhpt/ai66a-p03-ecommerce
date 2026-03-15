package com.oop.project.ui;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.oop.project.model.Order;
import com.oop.project.repository.OrderRepository;
import com.oop.project.repository.impl.OrderRepositoryImpl;

public class TestOrderRepository {

    public static void main(String[] args) {

        OrderRepository repo = new OrderRepositoryImpl();

        System.out.println("=== FIND ALL ===");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n=== FIND BY ID ===");
        System.out.println(repo.findById(1).orElse(null));

        Order order = new Order(
                0,
                1,
                null,
                new BigDecimal("0.10"),
                new BigDecimal("0.00"),
                "No discount",
                "PENDING",
                new Timestamp(System.currentTimeMillis()),
                new BigDecimal("1000.00"),
                new BigDecimal("1100.00")
        );

        System.out.println("\n=== SAVE ===");
        System.out.println("Saved: " + repo.save(order));

        System.out.println("\n=== FIND ALL AFTER SAVE ===");
        repo.findAll().forEach(System.out::println);
    }
}