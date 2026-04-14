package com.oop.project.service.impl;

import com.oop.project.exception.DuplicateException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.AuditLog;
import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.model.User;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.CustomerRepository;
import com.oop.project.repository.interfaces.OrderRepository;
import com.oop.project.service.interfaces.ICustomerService;
import com.oop.project.util.Validator;

import java.sql.Timestamp;
import java.util.List;

public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepo;
    private final OrderRepository orderRepo;
    private final AuditLogRepository auditRepo;

    public CustomerServiceImpl(CustomerRepository customerRepo, OrderRepository orderRepo,
            AuditLogRepository auditRepo) {
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.auditRepo = auditRepo;
    }

    private void log(User actor, String action, String targetId) {
        if (actor == null)
            return;
        try {
            AuditLog log = new AuditLog();
            log.setUser(actor);
            log.setActions(action);
            log.setTargetType("CUSTOMER");
            log.setTargetId(targetId);
            auditRepo.insert(log);
        } catch (Exception e) {
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    @Override
    public boolean addCustomer(Customer c, User actor) {
        validateCustomer(c);
        if (customerRepo.isPhoneExists(c.getPhone(), -1)) {
            throw new DuplicateException("Phone number '" + c.getPhone() + "' already existed!");
        }
        if (customerRepo.isEmailExists(c.getEmail(), -1)) {
            throw new DuplicateException("Email '" + c.getEmail() + "' already existed!");
        }
        if (c.getCreatedDate() == null) {
            c.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        }
        boolean ok = customerRepo.insert(c);
        if (ok) {
            log(actor, "CREATE_CUSTOMER", String.valueOf(c.getCustomerId()));
        }
        return ok;
    }

    @Override
    public boolean updateCustomer(Customer c, User actor) {
        validateCustomer(c);
        if (customerRepo.isPhoneExists(c.getPhone(), c.getCustomerId())) {
            throw new DuplicateException("Phone number '" + c.getPhone() + "' already existed!");
        }
        if (customerRepo.isEmailExists(c.getEmail(), c.getCustomerId())) {
            throw new DuplicateException("Email '" + c.getEmail() + "' already existed!");
        }
        boolean ok = customerRepo.update(c);
        if (ok) {
            log(actor, "UPDATE_CUSTOMER", String.valueOf(c.getCustomerId()));
        }
        return ok;
    }

    @Override
    public boolean deleteCustomer(int id, User actor) {
        List<Order> orders = orderRepo.findByCustomerId(id);
        if (orders != null && !orders.isEmpty()) {
            Customer c = customerRepo.findById(id);
            String name = (c != null) ? c.getCustomerName() : String.valueOf(id);
            throw new ValidationException(
                "Cannot delete customer \"" + name + "\": they have " + orders.size() + " order(s) on record.");
        }
        boolean ok = customerRepo.delete(id);
        if (ok) {
            log(actor, "DELETE_CUSTOMER", String.valueOf(id));
        }
        return ok;
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