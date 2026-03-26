package com.oop.project.service.impl;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Customer;
import com.oop.project.repository.CustomerRepository;
import com.oop.project.service.CustomerService;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> getCustomerById(int id) {
        if (id <= 0) {
            System.out.println("Customer ID must be greater than 0.");
            return Optional.empty();
        }
        return customerRepository.findById(id);
    }

    @Override
    public boolean createCustomer(Customer customer) {
        if (!validateCustomer(customer)) return false;
        if (customerRepository.findById(customer.getCustomerId()).isPresent()) {
            System.out.println("Customer already exists.");
            return false;
        }
        return customerRepository.save(customer);
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        if (!validateCustomer(customer)) return false;
        if (customerRepository.findById(customer.getCustomerId()).isEmpty()) {
            System.out.println("Customer not found.");
            return false;
        }
        return customerRepository.update(customer);
    }

    @Override
    public boolean deleteCustomer(int id) {
        if (id <= 0) {
            System.out.println("Customer ID must be greater than 0.");
            return false;
        }
        if (customerRepository.findById(id).isEmpty()) {
            System.out.println("Customer not found.");
            return false;
        }
        return customerRepository.deleteById(id);
    }

    private boolean validateCustomer(Customer customer) {
        if (customer == null) {
            System.out.println("Customer cannot be null.");
            return false;
        }
        if (customer.getCustomerId() <= 0) {
            System.out.println("Customer ID must be greater than 0.");
            return false;
        }
        if (customer.getCustomerName() == null || customer.getCustomerName().trim().isEmpty()) {
            System.out.println("Customer name cannot be empty.");
            return false;
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            System.out.println("Phone cannot be empty.");
            return false;
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            System.out.println("Email cannot be empty.");
            return false;
        }
        if (!customer.getEmail().contains("@")) {
            System.out.println("Email format is invalid.");
            return false;
        }
        if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            System.out.println("Address cannot be empty.");
            return false;
        }
        return true;
    }
}