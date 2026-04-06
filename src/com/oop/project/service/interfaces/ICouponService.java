package com.oop.project.service.interfaces;

import com.oop.project.model.Coupon;
import java.math.BigDecimal;
import java.util.List;

public interface ICouponService {
    List<Coupon> getActiveCoupons();
    boolean addCoupon(Coupon c);
    Coupon validateCoupon(String code, BigDecimal orderTotal);
}