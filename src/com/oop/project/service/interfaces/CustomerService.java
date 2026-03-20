package com.oop.project.service.interfaces;

import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.service.exception.ServiceException;
import java.util.List;

/**
 * Service interface for customer management operations.
 * Handles CRUD operations, search, and customer order history.
 * 
 * @author Service Team
 * @version 1.0
 */
public interface CustomerService {
    
    /**
     * Creates a new customer with validation.
     * Validates email format, phone format, and required fields.
     * 
     * @param customer the Customer object to create
     * @return the created Customer with generated ID
     * @throws ServiceException if validation fails or customer already exists
     */
    Customer createCustomer(Customer customer) throws ServiceException;
    
    /**
     * Updates existing customer information.
     * 
     * @param customer the Customer object with updated information
     * @return the updated Customer object
     * @throws ServiceException if customer not found or validation fails
     */
    Customer updateCustomer(Customer customer) throws ServiceException;
    
    /**
     * Deletes a customer by ID.
     * Checks if customer has active orders before deletion.
     * 
     * @param customerId the ID of customer to delete
     * @return true if deletion successful
     * @throws ServiceException if customer has active orders or not found
     */
    boolean deleteCustomer(int customerId) throws ServiceException;
    
    /**
     * Retrieves a customer by ID.
     * 
     * @param customerId the customer ID
     * @return Customer object if found, null otherwise
     */
    Customer getCustomerById(int customerId);
    
    /**
     * Retrieves all customers in the system.
     * 
     * @return List of all customers
     */
    List<Customer> getAllCustomers();
    
    /**
     * Searches customers by keyword (name, phone, or email).
     * Uses partial matching with LIKE SQL operator.
     * 
     * @param keyword the search keyword
     * @return List of matching customers
     */
    List<Customer> searchCustomers(String keyword);
    
    /**
     * Retrieves complete order history for a customer.
     * 
     * @param customerId the customer ID
     * @return List of orders placed by this customer
     */
    List<Order> getCustomerOrderHistory(int customerId);
    
    /**
     * Counts total number of customers.
     * 
     * @return total customer count
     */
    int getTotalCustomerCount();
}
