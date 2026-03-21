package com.oop.project.ui;

import java.sql.Timestamp;

import com.oop.project.model.Customer;
import com.oop.project.repository.CustomerRepository;
import com.oop.project.repository.impl.CustomerRepositoryImpl;

public class TestCustomerRepository {
    public static void main(String[] args) {

        CustomerRepository repo = new CustomerRepositoryImpl();

        System.out.println("=== CUSTOMER: FIND ALL ===");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n=== CUSTOMER: FIND BY ID (1) ===");
        System.out.println(repo.findById(1).orElse(null));

        System.out.println("\n=== CUSTOMER: SAVE ===");
        Customer c = new Customer(
                0,
                "Test Customer " + System.currentTimeMillis(),
                "0900000000",
                "test" + System.currentTimeMillis() + "@gmail.com",
                "Ha Noi",
                new Timestamp(System.currentTimeMillis())
        );
        System.out.println("Saved: " + repo.save(c));

        System.out.println("\n=== CUSTOMER: FIND ALL AFTER SAVE ===");
        repo.findAll().forEach(System.out::println);
    }
}