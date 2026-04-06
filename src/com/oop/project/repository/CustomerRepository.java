package com.oop.project.repository;

import com.oop.project.model.Customer;

import java.util.List;

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