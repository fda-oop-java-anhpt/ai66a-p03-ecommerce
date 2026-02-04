package com.oop.project.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Coupon {
    private String couponCode;
    private BigDecimal discountValue;
    private String discountType;   // Percent hoặc Fixed
    private BigDecimal minOrderValue;
    private Timestamp createdDate;   // Dùng created_at theo DB
    private Date expiryDate;
    private boolean isActive;

    public Coupon(String couponCode, BigDecimal discountValue, String discountType, 
                  BigDecimal minOrderValue, Timestamp createdDate, Date expiryDate, boolean isActive) {
        this.couponCode = couponCode;
        this.discountValue = discountValue;
        this.discountType = discountType;
        this.minOrderValue = minOrderValue;
        this.createdDate = createdDate;
        this.expiryDate = expiryDate;
        this.isActive = isActive;
    }
}