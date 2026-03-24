package com.oop.project.service.impl;

import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.CustomerRepository;
import com.oop.project.repository.OrderRepository;
import com.oop.project.repository.impl.CustomerRepositoryImpl;
import com.oop.project.repository.impl.OrderRepositoryImpl;
import com.oop.project.service.interfaces.CustomerService;
import com.oop.project.util.ValidationRules;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CustomerService.
 *
 * FR-1: Customer Management
 *
 * Responsibilities:
 * - CRUD operations on Customer records (FR-1.1)
 * - Validate phone number and email format (FR-1.2)
 * - Search customers by name or phone (FR-1.3)
 * - Display customer order history (FR-1.4)
 * - Enforce ADMIN role for delete operations
 *
 * @author Lan - Service Layer
 */
public class CustomerServiceImpl implements CustomerService {

    // ── Dependencies ──────────────────────────────────────────────
    private final CustomerRepository customerRepository;
    private final OrderRepository    orderRepository;

    // ── Constructor ───────────────────────────────────────────────
    public CustomerServiceImpl() {
        this.customerRepository = new CustomerRepositoryImpl();
        this.orderRepository    = new OrderRepositoryImpl();
    }

    public CustomerServiceImpl(CustomerRepository customerRepository,
                                OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository    = orderRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> getCustomerById(int customerId) {
        return customerRepository.findById(customerId);
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE — FR-1.1 + FR-1.2
    // ─────────────────────────────────────────────────────────────

    /**
     * Validate and save a new customer.
     *
     * Validation (FR-1.2):
     *  - Name must not be blank and within length limits
     *  - Phone must match PHONE_PATTERN
     *  - Email must match EMAIL_PATTERN
     */
    @Override
    public boolean addCustomer(Customer customer) {
        validateCustomer(customer);  // throws IllegalArgumentException if invalid
        return customerRepository.save(customer);
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE — FR-1.1 + FR-1.2
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean updateCustomer(Customer customer) {
        // Make sure the customer exists
        customerRepository.findById(customer.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Customer not found: ID " + customer.getCustomerId()));

        validateCustomer(customer);
        return customerRepository.update(customer);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE — FR-1.1 (ADMIN only)
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean deleteCustomer(int customerId, User actor) {
        // Enforce ADMIN-only permission
        if (actor == null || actor.getUserRole() != UserRole.ADMIN) {
            throw new SecurityException("DELETE_CUSTOMER requires ADMIN role.");
        }

        customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Customer not found: ID " + customerId));

        return customerRepository.deleteById(customerId);
    }

    // ─────────────────────────────────────────────────────────────
    // SEARCH — FR-1.3
    // ─────────────────────────────────────────────────────────────

    /**
     * Search customers by name or phone (case-insensitive, partial match).
     */
    @Override
    public List<Customer> searchCustomer(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllCustomers();

        String lower = keyword.trim().toLowerCase();
        return customerRepository.findAll().stream()
            .filter(c ->
                c.getCustomerName().toLowerCase().contains(lower) ||
                c.getPhone().contains(keyword.trim())
            )
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // ORDER HISTORY — FR-1.4
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Order> getCustomerOrderHistory(int customerId) {
        // Ensure customer exists first
        customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Customer not found: ID " + customerId));

        return orderRepository.findAll().stream()
            .filter(o -> o.getCustomer() != null &&
                         o.getCustomer().getCustomerId() == customerId)
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE VALIDATION HELPER — FR-1.2
    // ─────────────────────────────────────────────────────────────

    /**
     * Validate customer name, phone, and email.
     * Throws IllegalArgumentException with a clear message if any field is invalid.
     */
    private void validateCustomer(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Customer cannot be null.");

        // Validate name
        String name = customer.getCustomerName();
        if (name == null || name.trim().isEmpty() ||
            name.trim().length() < ValidationRules.MIN_NAME_LENGTH ||
            name.trim().length() > ValidationRules.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "Customer name must be between " +
                ValidationRules.MIN_NAME_LENGTH + " and " +
                ValidationRules.MAX_NAME_LENGTH + " characters.");
        }

        // Validate phone (FR-1.2)
        String phone = customer.getPhone();
        if (phone == null || !ValidationRules.PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException(
                "Invalid phone number format. Expected Vietnamese format (e.g., 0912345678).");
        }

        // Validate email (FR-1.2)
        String email = customer.getEmail();
        if (email == null || !ValidationRules.EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(
                "Invalid email format. Expected: user@domain.com");
        }
    }
}
