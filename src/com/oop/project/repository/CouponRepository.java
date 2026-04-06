package com.oop.project.repository;

import com.oop.project.model.Coupon;

import java.util.List;

public interface CouponRepository {
    List<Coupon> findAll();
    Coupon findByCode(String code);
    List<Coupon> findActiveCoupons();
    boolean insert(Coupon c);
    boolean update(Coupon c);
}