package com.oop.project.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    private int orderId;
    private Integer customerId;
    private String couponCode;
    private BigDecimal taxRate;
    private BigDecimal discountAmount;
    private String discountInfo;
    private String status;
    private Timestamp orderDate;
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

    public int getOrderId() { return orderId; }
    public Integer getCustomerId() { return customerId; }
    public String getCouponCode() { return couponCode; }
    public BigDecimal getTaxRate() { return taxRate; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public String getDiscountInfo() { return discountInfo; }
    public String getStatus() { return status; }
    public Timestamp getOrderDate() { return orderDate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getFinalTotal() { return finalTotal; }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", couponCode='" + couponCode + '\'' +
                ", taxRate=" + taxRate +
                ", discountAmount=" + discountAmount +
                ", discountInfo='" + discountInfo + '\'' +
                ", status='" + status + '\'' +
                ", orderDate=" + orderDate +
                ", subtotal=" + subtotal +
                ", finalTotal=" + finalTotal +
                '}';
    }
}