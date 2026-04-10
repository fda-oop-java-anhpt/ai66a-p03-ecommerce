package com.oop.project.service.impl;

import com.oop.project.exception.DuplicateException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.repository.interfaces.CustomerRepository;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.service.interfaces.ICustomerService;
import com.oop.project.util.Validator;

import java.sql.Timestamp;
import java.util.List;

public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepo;
    private final OrderRepository orderRepo;

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
        validateCustomer(c);
        if (customerRepo.isPhoneExists(c.getPhone(), -1)) {
            throw new DuplicateException("Số điện thoại '" + c.getPhone() + "' đã tồn tại!");
        }
        if (customerRepo.isEmailExists(c.getEmail(), -1)) {
            throw new DuplicateException("Email '" + c.getEmail() + "' đã tồn tại!");
        }
        if (c.getCreatedDate() == null) {
            c.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        }
        return customerRepo.insert(c);
    }

    @Override
    public boolean updateCustomer(Customer c) {
        validateCustomer(c);
        if (customerRepo.isPhoneExists(c.getPhone(), c.getCustomerId())) {
            throw new DuplicateException("Số điện thoại '" + c.getPhone() + "' đã tồn tại ở khách hàng khác!");
        }
        if (customerRepo.isEmailExists(c.getEmail(), c.getCustomerId())) {
            throw new DuplicateException("Email '" + c.getEmail() + "' đã tồn tại ở khách hàng khác!");
        }
        return customerRepo.update(c);
    }

    @Override
    public boolean deleteCustomer(int id) {
        return customerRepo.delete(id);
    }

    @Override
    public List<Customer> search(String keyword) {
        if (Validator.checkEmpty(keyword)) {
            return customerRepo.findAll();
        }
        return customerRepo.searchByNameOrPhone(keyword.trim());
    }

    @Override
    public List<Order> getOrderHistory(int customerId) {
        return orderRepo.findByCustomerId(customerId);
    }

    private void validateCustomer(Customer c) {
        if (c == null) {
            throw new ValidationException("Customer data must not be null.");
        }
        if (Validator.checkEmpty(c.getCustomerName())) {
            throw new ValidationException("Customer name must not be empty.");
        }
        if (!Validator.isValidPhone(c.getPhone())) {
            throw new ValidationException("Invalid phone number. Must be 10-11 digits.");
        }
        if (!Validator.isValidEmail(c.getEmail())) {
            throw new ValidationException("Invalid email format.");
        }
    }
}