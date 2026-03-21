package com.oop.project.ui;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.model.OrderStatus;
import com.oop.project.repository.CustomerRepository;
import com.oop.project.repository.OrderRepository;
import com.oop.project.repository.impl.CustomerRepositoryImpl;
import com.oop.project.repository.impl.OrderRepositoryImpl;

public class TestOrderRepository {
    public static void main(String[] args) {

        OrderRepository orderRepo = new OrderRepositoryImpl();
        CustomerRepository customerRepo = new CustomerRepositoryImpl();

        System.out.println("=== ORDER: FIND ALL ===");
        orderRepo.findAll().forEach(System.out::println);

        System.out.println("\n=== ORDER: FIND BY ID (1) ===");
        System.out.println(orderRepo.findById(1).orElse(null));

        System.out.println("\n=== ORDER: SAVE ===");
        Customer customer = customerRepo.findById(1).orElse(null);
        if (customer == null) {
            System.out.println("No customer id=1 found. Please seed customers first.");
            return;
        }

        Order order = new Order(
                0,
                customer,
                null,
                new BigDecimal("8.00"),
                new BigDecimal("0.00"),
                "No discount",
                OrderStatus.PENDING,
                new BigDecimal("1000000.00"),
                new BigDecimal("1080000.00"),
                new Timestamp(System.currentTimeMillis())
        );

        System.out.println("Saved: " + orderRepo.save(order));

        System.out.println("\n=== ORDER: FIND ALL AFTER SAVE ===");
        orderRepo.findAll().forEach(System.out::println);
    }
}