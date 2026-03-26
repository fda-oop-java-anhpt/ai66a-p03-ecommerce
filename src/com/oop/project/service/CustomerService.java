package com.oop.project.service;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Customer;

public interface CustomerService {
    List<Customer> getAllCustomers();
    Optional<Customer> getCustomerById(int id);
    boolean createCustomer(Customer customer);
    boolean updateCustomer(Customer customer);
    boolean deleteCustomer(int id);
}