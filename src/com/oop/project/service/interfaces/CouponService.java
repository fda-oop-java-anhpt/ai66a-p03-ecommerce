package com.oop.project.service.interfaces;

import com.oop.project.model.Coupon;
import com.oop.project.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * FR-4.1: Coupon codes with percentage or fixed discount
 * FR-4.2: Validate coupon expiration dates
 *
 * Handles all coupon-related business logic:
 * - Validate coupon codes and expiry dates
 * - Calculate discount amounts (Percent or Fixed)
 * - CRUD management for coupon records
 *
 * DiscountType enum values: Percent | Fixed
 *
 * @author Lan - Service Layer
 */
public interface CouponService {

    /**
     * Retrieve all coupons in the system.
     *
     * @return list of all Coupon objects
     */
    List<Coupon> getAllCoupons();

    /**
     * Find a coupon by its code.
     *
     * @param couponCode  the coupon code string (e.g., "SAVE10")
     * @return Optional<Coupon>, empty if not found
     */
    Optional<Coupon> getCouponByCode(String couponCode);

    /**
     * Validate a coupon code — checks existence, active status, and expiry.
     *
     * FR-4.2: The system shall validate coupon expiration dates.
     *
     * Validation steps:
     *  1. Coupon exists in database
     *  2. Coupon isActive == true
     *  3. Coupon expiryDate >= today's date
     *  4. orderTotal >= coupon.getMinOrderValue()
     *
     * @param couponCode   the code to validate
     * @param orderTotal   the current order total (to check minOrderValue requirement)
     * @return true if the coupon is valid and applicable
     */
    boolean validateCoupon(String couponCode, BigDecimal orderTotal);

    /**
     * Calculate and return the discount amount for a given coupon code.
     *
     * FR-4.1: Supports percentage (Percent) and fixed amount (Fixed) discounts.
     *
     * Calculation:
     *  - DiscountType.Percent: discount = orderTotal × (discountValue / 100)
     *  - DiscountType.Fixed:   discount = discountValue (capped at orderTotal)
     *
     * @param couponCode   the coupon code to apply
     * @param orderTotal   the subtotal before discount
     * @return the discount amount to subtract (returns ZERO if coupon is invalid)
     */
    BigDecimal getDiscountAmount(String couponCode, BigDecimal orderTotal);

    /**
     * Apply a coupon to an order total and return the final discounted amount.
     *
     * This is a convenience method that combines validateCoupon() + getDiscountAmount().
     * Returns the subtotal after discount (before tax).
     *
     * @param orderTotal   the original subtotal
     * @param couponCode   the coupon code to apply
     * @return the subtotal after discount, or original orderTotal if coupon is invalid
     */
    BigDecimal applyCoupon(BigDecimal orderTotal, String couponCode);

    /**
     * Create a new coupon in the system.
     *
     * FR-4.1: The system shall allow defining coupon codes with percentage or fixed discount.
     *
     * @param coupon  the Coupon object to save
     * @param actor   the currently logged-in User (ADMIN only)
     * @return true if saved successfully
     * @throws SecurityException if actor is not ADMIN
     * @throws IllegalArgumentException if coupon code format is invalid or already exists
     */
    boolean addCoupon(Coupon coupon, User actor);

    /**
     * Update an existing coupon's details.
     *
     * @param coupon  the Coupon object with updated fields
     * @param actor   the currently logged-in User (ADMIN only)
     * @return true if updated successfully
     * @throws SecurityException if actor is not ADMIN
     */
    boolean updateCoupon(Coupon coupon, User actor);

    /**
     * Delete a coupon by its code.
     *
     * @param couponCode  the code of the coupon to delete
     * @param actor       the currently logged-in User (ADMIN only)
     * @return true if deleted successfully
     * @throws SecurityException if actor is not ADMIN
     */
    boolean deleteCoupon(String couponCode, User actor);
}
