package com.oop.project.service.impl;

import com.oop.project.exception.CouponExpiredException;
import com.oop.project.exception.DuplicateException;
import com.oop.project.exception.ResourceNotFoundException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.Coupon;
import com.oop.project.model.DiscountType;
import com.oop.project.repository.interfaces.CouponRepository;
import com.oop.project.repository.impl.CouponRepositoryImpl;
import com.oop.project.service.interfaces.ICouponService;
import com.oop.project.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class CouponServiceImpl implements ICouponService {

    private final CouponRepository couponRepo;

    public CouponServiceImpl() {
        this.couponRepo = new CouponRepositoryImpl();
    }

    public CouponServiceImpl(CouponRepository couponRepo) {
        this.couponRepo = couponRepo;
    }

    @Override
    public List<Coupon> getActiveCoupons() {
        return couponRepo.findActiveCoupons();
    }

    @Override
    public boolean addCoupon(Coupon c) {
        if (c == null) throw new ValidationException("Coupon cannot be null.");
        // Validate coupon code format
        if (Validator.checkEmpty(c.getCouponCode()) || !Validator.isValidCouponCode(c.getCouponCode())) {
            throw new ValidationException("Invalid coupon code format. Must be uppercase letters and digits (4-20 chars).");
        }
        // Check duplicate
        Coupon existing = couponRepo.findByCode(c.getCouponCode());
        if (existing != null) {
            throw new DuplicateException("Coupon code already exists: " + c.getCouponCode());
        }
        // Validate discount value
        if (c.getDiscountValue() == null || c.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Discount value must be greater than 0.");
        }
        if (c.getDiscountType() == DiscountType.Percent && c.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException("Percentage discount cannot exceed 100%.");
        }
        // Validate expiry date (cannot be in past)
        if (c.getExpiryDate() != null && c.getExpiryDate().before(Date.valueOf(LocalDate.now()))) {
            throw new ValidationException("Expiry date cannot be in the past.");
        }
        // Default active
        if (!c.isActive()) c.setActive(true);
        return couponRepo.insert(c);
    }

    @Override
    public Coupon validateCoupon(String code, BigDecimal orderTotal) {
        if (Validator.checkEmpty(code)) {
            throw new CouponExpiredException("Coupon code is empty.");
        }
        Coupon coupon = couponRepo.findByCode(code.trim().toUpperCase());
        if (coupon == null) {
            throw new CouponExpiredException("Coupon '" + code + "' not found.");
        }
        if (!coupon.isActive()) {
            throw new CouponExpiredException("Coupon '" + code + "' is no longer active.");
        }
        Date today = Date.valueOf(LocalDate.now());
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().before(today)) {
            throw new CouponExpiredException("Coupon '" + code + "' has expired on " + coupon.getExpiryDate());
        }
        if (orderTotal != null && coupon.getMinOrderValue() != null && orderTotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new CouponExpiredException("Order total must be at least " + coupon.getMinOrderValue() + " to use coupon '" + code + "'.");
        }
        return coupon;
    }
}