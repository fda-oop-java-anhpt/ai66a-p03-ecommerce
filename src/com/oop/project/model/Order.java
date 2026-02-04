package com.oop.project.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    private int orderId;
    private Integer customerId; // Dùng Integer thay vì int vì DB cho phép NULL
    private String couponCode;
    private BigDecimal taxRate;    // Mặc định 8.00
    private BigDecimal discountAmount;
    private String discountInfo;
    private String status;         // PENDING, PAID, CANCELLED
    private Timestamp orderDate;   // Dùng order_date theo DB
    private BigDecimal subtotal;
    private BigDecimal finalTotal;

    public Order(int orderId, Integer customerId, String couponCode, BigDecimal taxRate, 
                 BigDecimal discountAmount, String discountInfo, String status, 
                 Timestamp orderDate, BigDecimal subtotal, BigDecimal finalTotal) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.couponCode = couponCode;
        this.taxRate = taxRate;
        this.discountAmount = discountAmount;
        this.discountInfo = discountInfo;
        this.status = status;
        this.orderDate = orderDate;
        this.subtotal = subtotal;
        this.finalTotal = finalTotal;
    }
}