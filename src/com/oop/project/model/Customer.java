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
        setCustomerName(customerName);
        setPhone(phone);
        setEmail(email);
        this.address = address;
        this.createdDate = createdDate;
    }
    public Customer(){}

    // Getters and Setters
    public int getCustomerId(){ 
        return customerId; 
    }
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    public String getCustomerName(){ 
        return customerName; 
    }
    public void setCustomerName(String customerName) {
        if (customerName == null || customerName.isBlank()){
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        this.customerName = customerName;
    }
    public String getPhone(){ 
        return phone; 
    }
    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()){
            throw new IllegalArgumentException("Phone cannot be null or empty");
        }
        this.phone = phone;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        this.email = email;
    }
    public String getAddress(){ 
        return address; 
    }
    public void setAddress(String address){ 
        this.address = address; 
    }
    public Timestamp getCreatedDate(){ 
        return createdDate;
    }
    public void setCreatedDate(Timestamp createdDate){ 
        this.createdDate = createdDate; 
    }
    @Override 
    public String toString(){
        return "Customer{" + "id=" + customerId + ", name='" + customerName + "'" + ", phone='" + phone + "'" + '}';

    }
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return customerId == customer.customerId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(customerId);
    }
}
