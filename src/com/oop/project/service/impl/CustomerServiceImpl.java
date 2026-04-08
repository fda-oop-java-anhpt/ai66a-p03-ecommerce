package com.oop.project.service.impl;

import com.oop.project.exception.DuplicateException;
import com.oop.project.exception.ResourceNotFoundException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.repository.interfaces.CustomerRepository;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.repository.impl.CustomerRepositoryImpl;
import com.oop.project.repository.impl.OrderRepositoryImpl;
import com.oop.project.service.interfaces.ICustomerService;
import com.oop.project.util.Validator;

import java.util.List;

public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepo;
    private final OrderRepository orderRepo;

    public CustomerServiceImpl() {
        this.customerRepo = new CustomerRepositoryImpl();
        this.orderRepo = new OrderRepositoryImpl();
    }

    public CustomerServiceImpl(CustomerRepository customerRepo, OrderRepository orderRepo) {
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    @Override
    public boolean addCustomer(Customer c) {
        if (c == null) throw new ValidationException("Customer cannot be null.");
        // Validate name
        if (Validator.checkEmpty(c.getCustomerName()) || c.getCustomerName().trim().length() < Validator.MIN_NAME_LENGTH ||
                c.getCustomerName().trim().length() > Validator.MAX_NAME_LENGTH) {
            throw new ValidationException("Customer name must be between " + Validator.MIN_NAME_LENGTH + " and " + Validator.MAX_NAME_LENGTH + " characters.");
        }
        // Validate phone
        if (!Validator.isValidPhone(c.getPhone())) {
            throw new ValidationException("Invalid phone number format. Expected Vietnamese format (e.g., 0912345678).");
        }
        // Validate email
        if (!Validator.isValidEmail(c.getEmail())) {
            throw new ValidationException("Invalid email format.");
        }
        // Check duplicate phone
        if (customerRepo.isPhoneExists(c.getPhone(), 0)) {
            throw new DuplicateException("Phone number already exists: " + c.getPhone());
        }
        // Check duplicate email
        if (customerRepo.isEmailExists(c.getEmail(), 0)) {
            throw new DuplicateException("Email already exists: " + c.getEmail());
        }
        return customerRepo.insert(c);
    }

    @Override
    public boolean updateCustomer(Customer c) {
        if (c == null) throw new ValidationException("Customer cannot be null.");
        Customer existing = customerRepo.findById(c.getCustomerId());
        if (existing == null) {
            throw new ResourceNotFoundException("Customer not found: ID " + c.getCustomerId());
        }
        // Validate fields
        if (Validator.checkEmpty(c.getCustomerName()) || c.getCustomerName().trim().length() < Validator.MIN_NAME_LENGTH ||
                c.getCustomerName().trim().length() > Validator.MAX_NAME_LENGTH) {
            throw new ValidationException("Customer name must be between " + Validator.MIN_NAME_LENGTH + " and " + Validator.MAX_NAME_LENGTH + " characters.");
        }
        if (!Validator.isValidPhone(c.getPhone())) {
            throw new ValidationException("Invalid phone number format.");
        }
        if (!Validator.isValidEmail(c.getEmail())) {
            throw new ValidationException("Invalid email format.");
        }
        // Check duplicate phone excluding current
        if (customerRepo.isPhoneExists(c.getPhone(), c.getCustomerId())) {
            throw new DuplicateException("Phone number already exists: " + c.getPhone());
        }
        if (customerRepo.isEmailExists(c.getEmail(), c.getCustomerId())) {
            throw new DuplicateException("Email already exists: " + c.getEmail());
        }
        return customerRepo.update(c);
    }

    @Override
    public boolean deleteCustomer(int id) {
        Customer existing = customerRepo.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Customer not found: ID " + id);
        }
        return customerRepo.delete(id);
    }

    @Override
    public List<Customer> search(String keyword) {
        if (Validator.checkEmpty(keyword)) {
            return getAllCustomers();
        }
        return customerRepo.searchByNameOrPhone(keyword.trim());
    }

    @Override
    public List<Order> getOrderHistory(int customerId) {
        Customer existing = customerRepo.findById(customerId);
        if (existing == null) {
            throw new ResourceNotFoundException("Customer not found: ID " + customerId);
        }
        return orderRepo.findByCustomerId(customerId);
    }
}