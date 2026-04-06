package com.oop.project.service.interfaces;

import com.oop.project.model.Customer;
import com.oop.project.model.Order;
import java.util.List;

public interface ICustomerService {
    List<Customer> getAllCustomers();
    boolean addCustomer(Customer c);
    boolean updateCustomer(Customer c);
    boolean deleteCustomer(int id);
    List<Customer> search(String keyword);
    List<Order> getOrderHistory(int customerId);
}