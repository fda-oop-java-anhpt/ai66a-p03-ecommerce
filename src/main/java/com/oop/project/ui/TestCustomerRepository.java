package com.oop.project.ui;

import java.sql.Timestamp;

import com.oop.project.model.Customer;
import com.oop.project.repository.CustomerRepository;
import com.oop.project.repository.impl.CustomerRepositoryImpl;

public class TestCustomerRepository {

    public static void main(String[] args) {

        CustomerRepository repo = new CustomerRepositoryImpl();

        System.out.println("=== FIND ALL ===");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n=== FIND BY ID ===");
        System.out.println(repo.findById(1).orElse(null));

        Customer customer = new Customer(
                0,
                "Test Customer",
                "0123456789",
                "testcustomer@gmail.com",
                "Ha Noi",
                new Timestamp(System.currentTimeMillis())
        );

        System.out.println("\n=== SAVE ===");
        System.out.println("Saved: " + repo.save(customer));

        System.out.println("\n=== FIND ALL AFTER SAVE ===");
        repo.findAll().forEach(System.out::println);
    }
}