package com.oop.project.service.interfaces;

import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import com.oop.project.model.User;
import java.util.List;

public interface ICustomerService {
    List<Customer> getAllCustomers();
    List<Customer> getAllActiveCustomers();
    boolean addCustomer(Customer c, User actor);
    boolean updateCustomer(Customer c, User actor);
    boolean deleteCustomer(int id, User actor);
    boolean setCustomerStatus(int id, boolean isActive, User actor);
    List<Customer> search(String keyword);
    List<Order> getOrderHistory(int customerId);
}