package com.oop.project.model;

import java.sql.Timestamp;

public class Customer {
    private int customerId;
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private Timestamp createdDate;

    public Customer(int customerId, String customerName, String phone, String email, String address, Timestamp createdDate) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdDate = createdDate;
    }

    // Getters
    public int getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }

}
