package com.oop.project.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

public class Coupon {
    private String couponCode;
    private BigDecimal discountValue;
    private DiscountType discountType;   
    private BigDecimal minOrderValue;
    private Timestamp createdDate;   
    private Date expiryDate;
    private boolean isActive;

    public Coupon(){}



    public Coupon(String couponCode, BigDecimal discountValue, DiscountType discountType, 
                  BigDecimal minOrderValue, Timestamp createdDate, Date expiryDate, boolean isActive) {
        this.couponCode = couponCode;
        this.discountValue = discountValue;
        this.discountType = discountType;
        this.minOrderValue = minOrderValue;
        this.createdDate = createdDate;
        this.expiryDate = expiryDate;
        this.isActive = isActive;
    }
    public String getCouponCode(){
        return couponCode;
    }
    public void setCouponCode(String couponCode){
        this.couponCode = couponCode;
    }
    public BigDecimal getDiscountValue(){ 
        return discountValue; 
    }
    public void setDiscountValue(BigDecimal discountValue){ 
        if (discountValue.compareTo(BigDecimal.ZERO) <=0 ){
            throw new IllegalArgumentException("Error: Discount value must be greater than 0");
        }
        this.discountValue = discountValue; 
    }

    public DiscountType getDiscountType(){ 
        return discountType; 
    }
    public void setDiscountType(DiscountType discountType) { 
        this.discountType = discountType; 
    }

    public BigDecimal getMinOrderValue(){ 
        return minOrderValue; 
    }
    public void setMinOrderValue(BigDecimal minOrderValue){ 
        if (minOrderValue.compareTo(BigDecimal.ZERO) <0){
            throw new IllegalArgumentException("Error: Min order value cannot be a negative number");
        }
        this.minOrderValue = minOrderValue; 
    }

    public Timestamp getCreatedDate(){ 
        return createdDate; 
    }
    public void setCreatedDate(Timestamp createdDate) { 
        this.createdDate = createdDate; 
    }

    public Date getExpiryDate() { 
        return expiryDate; 
    }
    public void setExpiryDate(Date expiryDate) { 
        this.expiryDate = expiryDate; 
    }

    public boolean isActive() { 
        return isActive; 
    }
    public void setActive(boolean active) {
        isActive = active; 
    }

    @Override
    public String toString(){
        return "Coupon{" + "code=" + couponCode + ", value=' " + discountValue + "'" + ", type='" + discountType + "'" +  ", active='" + isActive + "'";
    }
    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Coupon coupon = (Coupon) o;
        return Objects.equals(couponCode, coupon.couponCode);
    }
}