package com.oop.project.service.impl;

import com.oop.project.exception.CouponExpiredException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.Coupon;
import com.oop.project.repository.CouponRepository;
import com.oop.project.service.interfaces.ICouponService;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class CouponServiceImpl implements ICouponService {

    private final CouponRepository couponRepo;

    public CouponServiceImpl(CouponRepository couponRepo) {
        this.couponRepo = couponRepo;
    }

    @Override
    public List<Coupon> getActiveCoupons() {
        return couponRepo.findActiveCoupons();
    }

    @Override
    public boolean addCoupon(Coupon c) {
        if (c == null || c.getCouponCode() == null || c.getCouponCode().trim().isEmpty()) {
            throw new ValidationException("Coupon code must not be empty.");
        }
        return couponRepo.insert(c);
    }

    @Override
    public Coupon validateCoupon(String code, BigDecimal orderTotal) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Coupon code must not be empty.");
        }
        Coupon coupon = couponRepo.findByCode(code.trim());
        if (coupon == null) {
            throw new ValidationException("Coupon '" + code + "' not found.");
        }
        if (!coupon.isActive()) {
            throw new CouponExpiredException("Coupon '" + code + "' is no longer active.");
        }
        Date today = new Date(System.currentTimeMillis());
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().before(today)) {
            throw new CouponExpiredException("Coupon '" + code + "' has expired on " + coupon.getExpiryDate() + ".");
        }
        if (coupon.getMinOrderValue() != null && orderTotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new CouponExpiredException("Order total must be at least " + coupon.getMinOrderValue() + " to use coupon '" + code + "'.");
        }
        return coupon;
    }
}