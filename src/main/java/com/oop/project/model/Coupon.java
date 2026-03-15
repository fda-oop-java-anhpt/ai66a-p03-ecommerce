package com.oop.project.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Coupon {
    private String couponCode;
    private BigDecimal discountValue;
    private String discountType;
    private BigDecimal minOrderValue;
    private Timestamp createdDate;
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

    public String getCouponCode() { return couponCode; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public String getDiscountType() { return discountType; }
    public BigDecimal getMinOrderValue() { return minOrderValue; }
    public Timestamp getCreatedDate() { return createdDate; }
    public Date getExpiryDate() { return expiryDate; }
    public boolean isActive() { return isActive; }

    @Override
    public String toString() {
        return "Coupon{" +
                "couponCode='" + couponCode + '\'' +
                ", discountValue=" + discountValue +
                ", discountType='" + discountType + '\'' +
                ", minOrderValue=" + minOrderValue +
                ", createdDate=" + createdDate +
                ", expiryDate=" + expiryDate +
                ", isActive=" + isActive +
                '}';
    }
}