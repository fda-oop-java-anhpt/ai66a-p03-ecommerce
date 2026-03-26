package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Coupon;

public interface CouponRepository {
    List<Coupon> findAll();
    Optional<Coupon> findByCode(String code);
    boolean save(Coupon coupon);
    boolean update(Coupon coupon);
    boolean deleteByCode(String code);
}