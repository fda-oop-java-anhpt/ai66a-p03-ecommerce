package com.oop.project.service.impl;

import com.oop.project.model.Coupon;
import com.oop.project.model.DiscountType;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.CouponRepository;
import com.oop.project.repository.impl.CouponRepositoryImpl;
import com.oop.project.service.interfaces.CouponService;
import com.oop.project.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of CouponService.
 *
 * FR-4.1: Define coupon codes with percentage or fixed discount
 * FR-4.2: Validate coupon expiration dates
 *
 * Validation logic:
 *  - Coupon must exist in the database
 *  - Coupon must be active (isActive == true)
 *  - Coupon expiryDate must be >= today (FR-4.2)
 *  - Order total must meet coupon's minOrderValue
 *
 * @author Lan - Service Layer
 */
public class CouponServiceImpl implements CouponService {

    // ── Dependencies ──────────────────────────────────────────────
    private final CouponRepository couponRepository;

    // ── Constructor ───────────────────────────────────────────────
    public CouponServiceImpl() {
        this.couponRepository = new CouponRepositoryImpl();
    }

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Optional<Coupon> getCouponByCode(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) return Optional.empty();
        return couponRepository.findByCode(couponCode.trim().toUpperCase());
    }

    // ─────────────────────────────────────────────────────────────
    // FR-4.2: VALIDATE COUPON
    // ─────────────────────────────────────────────────────────────

    /**
     * Full coupon validation:
     *  1. Coupon exists
     *  2. isActive == true
     *  3. expiryDate >= today (FR-4.2)
     *  4. orderTotal >= minOrderValue
     */
    @Override
    public boolean validateCoupon(String couponCode, BigDecimal orderTotal) {
        if (couponCode == null || couponCode.trim().isEmpty()) return false;

        Optional<Coupon> opt = couponRepository.findByCode(couponCode.trim().toUpperCase());
        if (opt.isEmpty()) return false;

        Coupon coupon = opt.get();

        // Check active status
        if (!coupon.isActive()) return false;

        // Check expiry date (FR-4.2)
        if (coupon.getExpiryDate() != null) {
            Date today = Date.valueOf(LocalDate.now());
            if (coupon.getExpiryDate().before(today)) return false;
        }

        // Check minimum order value
        if (orderTotal != null && coupon.getMinOrderValue() != null) {
            if (orderTotal.compareTo(coupon.getMinOrderValue()) < 0) return false;
        }

        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // FR-4.1: CALCULATE DISCOUNT AMOUNT
    // ─────────────────────────────────────────────────────────────

    /**
     * Calculate the discount amount for a coupon code.
     *
     *  Percent: discount = orderTotal × (discountValue / 100)
     *  Fixed:   discount = discountValue (capped at orderTotal)
     */
    @Override
    public BigDecimal getDiscountAmount(String couponCode, BigDecimal orderTotal) {
        if (!validateCoupon(couponCode, orderTotal)) return BigDecimal.ZERO;

        Coupon coupon = couponRepository.findByCode(couponCode.trim().toUpperCase()).get();

        if (coupon.getDiscountType() == DiscountType.Percent) {
            // Percentage discount
            return orderTotal
                .multiply(coupon.getDiscountValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        } else {
            // Fixed discount — cannot exceed orderTotal
            BigDecimal fixedDiscount = coupon.getDiscountValue();
            return fixedDiscount.compareTo(orderTotal) > 0 ? orderTotal : fixedDiscount;
        }
    }

    /**
     * Convenience: apply coupon and return the subtotal AFTER discount.
     */
    @Override
    public BigDecimal applyCoupon(BigDecimal orderTotal, String couponCode) {
        BigDecimal discount = getDiscountAmount(couponCode, orderTotal);
        BigDecimal result   = orderTotal.subtract(discount);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD (ADMIN only for write operations)
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean addCoupon(Coupon coupon, User actor) {
        requireAdmin(actor);
        validateCouponData(coupon);

        // Check for duplicate coupon code
        if (couponRepository.findByCode(coupon.getCouponCode()).isPresent()) {
            throw new IllegalArgumentException(
                "Coupon code already exists: " + coupon.getCouponCode());
        }

        return couponRepository.save(coupon);
    }

    @Override
    public boolean updateCoupon(Coupon coupon, User actor) {
        requireAdmin(actor);
        couponRepository.findByCode(coupon.getCouponCode())
            .orElseThrow(() -> new IllegalArgumentException(
                "Coupon not found: " + coupon.getCouponCode()));
        validateCouponData(coupon);
        return couponRepository.update(coupon);
    }

    @Override
    public boolean deleteCoupon(String couponCode, User actor) {
        requireAdmin(actor);
        couponRepository.findByCode(couponCode)
            .orElseThrow(() -> new IllegalArgumentException(
                "Coupon not found: " + couponCode));
        return couponRepository.deleteByCode(couponCode);
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private void requireAdmin(User actor) {
        if (actor == null || actor.getUserRole() != UserRole.ADMIN) {
            throw new SecurityException("This action requires ADMIN role.");
        }
    }

    /**
     * Validate coupon fields:
     * - Code must match COUPON_CODE_PATTERN
     * - discountValue must be > 0
     * - If Percent type, discountValue must be <= 100
     * - expiryDate must not be in the past
     */
    private void validateCouponData(Coupon coupon) {
        if (coupon == null) throw new IllegalArgumentException("Coupon cannot be null.");

        // Validate coupon code format
        String code = coupon.getCouponCode();
        if (code == null || !Validator.COUPON_CODE_PATTERN.matcher(code.trim()).matches()) {
            throw new IllegalArgumentException(
                "Invalid coupon code format. Must be uppercase letters and digits (4-20 chars). E.g., SAVE10");
        }

        // Validate discount value
        if (coupon.getDiscountValue() == null ||
            coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0.");
        }

        // Percent discount cannot exceed 100%
        if (coupon.getDiscountType() == DiscountType.Percent &&
            coupon.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%.");
        }

        // ExpiryDate must not be in the past
        if (coupon.getExpiryDate() != null) {
            Date today = Date.valueOf(LocalDate.now());
            if (coupon.getExpiryDate().before(today)) {
                throw new IllegalArgumentException(
                    "Expiry date cannot be in the past: " + coupon.getExpiryDate());
            }
        }
    }
}
