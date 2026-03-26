package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Customer;

public interface CustomerRepository {
    List<Customer> findAll();
    Optional<Customer> findById(int id);
    boolean save(Customer customer);
    boolean update(Customer customer);
    boolean deleteById(int id);
}