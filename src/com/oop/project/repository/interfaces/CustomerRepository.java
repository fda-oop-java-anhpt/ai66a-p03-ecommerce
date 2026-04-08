package com.oop.project.repository.interfaces;

import java.util.List;

import com.oop.project.model.Customer;

public interface CustomerRepository {
    List<Customer> findAll();
    Customer findById(int id);
    List<Customer> searchByNameOrPhone(String keyword);
    boolean isPhoneExists(String phone, int excludeId);
    boolean isEmailExists(String email, int excludeId);
    boolean insert(Customer c);
    boolean update(Customer c);
    boolean delete(int id);
}