package com.oop.project.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.Coupon;
import com.oop.project.repository.CouponRepository;
import com.oop.project.service.CouponService;

public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Optional<Coupon> getCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            System.out.println("Coupon code cannot be empty.");
            return Optional.empty();
        }
        return couponRepository.findByCode(code);
    }

    @Override
    public boolean createCoupon(Coupon coupon) {
        if (!validateCoupon(coupon)) return false;
        if (couponRepository.findByCode(coupon.getCouponCode()).isPresent()) {
            System.out.println("Coupon already exists.");
            return false;
        }
        return couponRepository.save(coupon);
    }

    @Override
    public boolean updateCoupon(Coupon coupon) {
        if (!validateCoupon(coupon)) return false;
        if (couponRepository.findByCode(coupon.getCouponCode()).isEmpty()) {
            System.out.println("Coupon not found.");
            return false;
        }
        return couponRepository.update(coupon);
    }

    @Override
    public boolean deleteCoupon(String code) {
        if (code == null || code.trim().isEmpty()) {
            System.out.println("Coupon code cannot be empty.");
            return false;
        }
        if (couponRepository.findByCode(code).isEmpty()) {
            System.out.println("Coupon not found.");
            return false;
        }
        return couponRepository.deleteByCode(code);
    }

    private boolean validateCoupon(Coupon coupon) {
        if (coupon == null) {
            System.out.println("Coupon cannot be null.");
            return false;
        }
        if (coupon.getCouponCode() == null || coupon.getCouponCode().trim().isEmpty()) {
            System.out.println("Coupon code cannot be empty.");
            return false;
        }
        if (coupon.getDiscountValue() == null || coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Discount value must be greater than 0.");
            return false;
        }
        if (coupon.getDiscountType() == null) {
            System.out.println("Discount type cannot be null.");
            return false;
        }
        if (coupon.getMinOrderValue() == null || coupon.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Min order value cannot be negative.");
            return false;
        }
        Date expiryDate = coupon.getExpiryDate();
        if (expiryDate == null) {
            System.out.println("Expiry date cannot be null.");
            return false;
        }
        return true;
    }
}