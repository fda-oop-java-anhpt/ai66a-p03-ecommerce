package com.oop.project.service.impl;

import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.service.interfaces.CustomerService;
import com.oop.project.exception.ServiceException;
import com.oop.project.exception.ValidationException;
import com.oop.project.util.Validator;
import com.oop.project.util.ValidationRules;
// import com.oop.project.repository.CustomerRepository; // Uncomment Week 4
// import com.oop.project.repository.OrderRepository; // Uncomment Week 4
import java.util.*;

/**
 * Implementation of CustomerService interface.
 * Handles customer CRUD operations, search, and order history.
 * 
 * NOTE: Uses stub data for Week 1-3. Integrate with Repository in Week 4.
 * 
 * @author Service Team - Member 3
 * @version 1.0
 */
public class CustomerServiceImpl implements CustomerService {
    
    // TODO Week 4: Inject repositories via constructor
    // private final CustomerRepository customerRepository;
    // private final OrderRepository orderRepository;
    
    /**
     * Creates a new customer with validation.
     * 
     * @param customer the Customer object to create
     * @return the created Customer with generated ID
     * @throws ServiceException if validation fails or customer already exists
     */
    @Override
    public Customer createCustomer(Customer customer) throws ServiceException {
        try {
            // Validate customer data
            validateCustomerData(customer);
            
            // Check for duplicate email or phone
            // TODO Week 4: if (customerRepository.existsByEmail(customer.getEmail())) {
            //     throw new ValidationException("Email", "Email already exists");
            // }
            
            // TODO Week 4: Customer created = customerRepository.save(customer);
            // return created;
            
            // STUB: Return customer with generated ID
            return customer;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to create customer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates existing customer information.
     * 
     * @param customer the Customer object with updated information
     * @return the updated Customer object
     * @throws ServiceException if customer not found or validation fails
     */
    @Override
    public Customer updateCustomer(Customer customer) throws ServiceException {
        try {
            // Validate customer data
            Validator.validatePositiveId(customer.getCustomerId(), "Customer ID");
            validateCustomerData(customer);
            
            // TODO Week 4: Check if customer exists
            // if (!customerRepository.existsById(customer.getCustomerId())) {
            //     throw new ServiceException("Customer not found with ID: " + customer.getCustomerId());
            // }
            
            // TODO Week 4: return customerRepository.update(customer);
            
            // STUB
            return customer;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to update customer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes a customer by ID.
     * Checks if customer has active orders before deletion.
     * 
     * @param customerId the ID of customer to delete
     * @return true if deletion successful
     * @throws ServiceException if customer has active orders or not found
     */
    @Override
    public boolean deleteCustomer(int customerId) throws ServiceException {
        try {
            Validator.validatePositiveId(customerId, "Customer ID");
            
            // Check for active orders
            // TODO Week 4: List<Order> orders = orderRepository.findByCustomerId(customerId);
            // if (!orders.isEmpty()) {
            //     throw new ServiceException("Cannot delete customer with existing orders");
            // }
            
            // TODO Week 4: return customerRepository.deleteById(customerId);
            
            // STUB
            return true;
            
        } catch (Exception e) {
            throw new ServiceException("Failed to delete customer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves a customer by ID.
     * 
     * @param customerId the customer ID
     * @return Customer object if found, null otherwise
     */
    @Override
    public Customer getCustomerById(int customerId) {
        try {
            // TODO Week 4: return customerRepository.findById(customerId);
            
            // STUB
            return null;
        } catch (Exception e) {
            System.err.println("Error retrieving customer: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Retrieves all customers in the system.
     * 
     * @return List of all customers
     */
    @Override
    public List<Customer> getAllCustomers() {
        try {
            // TODO Week 4: return customerRepository.findAll();
            
            // STUB
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error retrieving customers: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Searches customers by keyword (name, phone, or email).
     * 
     * @param keyword the search keyword
     * @return List of matching customers
     */
    @Override
    public List<Customer> searchCustomers(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllCustomers();
            }
            
            // TODO Week 4: return customerRepository.searchByKeyword(keyword);
            
            // STUB
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error searching customers: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves complete order history for a customer.
     * 
     * @param customerId the customer ID
     * @return List of orders placed by this customer
     */
    @Override
    public List<Order> getCustomerOrderHistory(int customerId) {
        try {
            Validator.validatePositiveId(customerId, "Customer ID");
            
            // TODO Week 4: return orderRepository.findByCustomerId(customerId);
            
            // STUB
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error retrieving order history: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Counts total number of customers.
     * 
     * @return total customer count
     */
    @Override
    public int getTotalCustomerCount() {
        try {
            // TODO Week 4: return customerRepository.count();
            
            // STUB
            return 0;
        } catch (Exception e) {
            System.err.println("Error counting customers: " + e.getMessage());
            return 0;
        }
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    /**
     * Validates customer data before create/update operations.
     * 
     * @param customer the customer to validate
     * @throws ValidationException if validation fails
     */
    private void validateCustomerData(Customer customer) throws ValidationException {
        if (customer == null) {
            throw new ValidationException("Customer object cannot be null");
        }
        
        // Validate name
        Validator.validateRequired(customer.getCustomerName(), "Customer Name");
        Validator.validateLength(customer.getCustomerName(), "Customer Name", 
                               ValidationRules.MIN_NAME_LENGTH, ValidationRules.MAX_NAME_LENGTH);
        
        // Validate email
        Validator.validateEmail(customer.getEmail());
        
        // Validate phone
        Validator.validatePhone(customer.getPhone());
        
        // Address is optional but if provided, should not be empty
        if (customer.getAddress() != null && !customer.getAddress().trim().isEmpty()) {
            Validator.validateLength(customer.getAddress(), "Address", 5, 200);
        }
    }
}
