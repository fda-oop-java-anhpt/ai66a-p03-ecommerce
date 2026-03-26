package com.oop.project.service;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Coupon;

public interface CouponService {
    List<Coupon> getAllCoupons();
    Optional<Coupon> getCouponByCode(String code);
    boolean createCoupon(Coupon coupon);
    boolean updateCoupon(Coupon coupon);
    boolean deleteCoupon(String code);
}