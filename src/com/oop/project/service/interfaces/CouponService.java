package com.oop.project.service.interfaces;

import com.oop.project.model.Coupon;
import com.oop.project.service.exception.ServiceException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for coupon management and validation.
 * Handles coupon validation, expiry date checking, and discount calculation.
 * 
 * @author Service Team
 * @version 1.0
 */
public interface CouponService {
    
    /**
     * Creates a new coupon with validation.
     * 
     * @param coupon the Coupon object to create
     * @return the created Coupon
     * @throws ServiceException if coupon code already exists or validation fails
     */
    Coupon createCoupon(Coupon coupon) throws ServiceException;
    
    /**
     * Updates existing coupon information.
     * 
     * @param coupon the Coupon with updated information
     * @return the updated Coupon
     * @throws ServiceException if coupon not found or validation fails
     */
    Coupon updateCoupon(Coupon coupon) throws ServiceException;
    
    /**
     * Deletes a coupon by code.
     * 
     * @param couponCode the coupon code to delete
     * @return true if deletion successful
     * @throws ServiceException if coupon is in use or not found
     */
    boolean deleteCoupon(String couponCode) throws ServiceException;
    
    /**
     * Retrieves a coupon by code.
     * 
     * @param couponCode the coupon code
     * @return Coupon object if found, null otherwise
     */
    Coupon getCouponByCode(String couponCode);
    
    /**
     * Retrieves all coupons in the system.
     * 
     * @return List of all coupons
     */
    List<Coupon> getAllCoupons();
    
    /**
     * Validates if a coupon is usable.
     * Checks:
     * - Coupon exists
     * - Not expired (expiryDate >= current date)
     * - Is active (isActive = true)
     * - Meets minimum order value requirement
     * 
     * @param couponCode the coupon code to validate
     * @param orderTotal the current order total
     * @return true if coupon is valid and can be used
     * @throws ServiceException if coupon is expired, inactive, or order doesn't meet minimum
     */
    boolean validateCoupon(String couponCode, BigDecimal orderTotal) throws ServiceException;
    
    /**
     * Applies a coupon to an order total and calculates discount amount.
     * Supports two discount types:
     * - "Percent": discount = orderTotal × (discountValue / 100)
     * - "Fixed": discount = discountValue (flat amount)
     * 
     * @param orderTotal the order subtotal before discount
     * @param couponCode the coupon code to apply
     * @return the discount amount to subtract from order total
     * @throws ServiceException if coupon is invalid or cannot be applied
     */
    BigDecimal applyCoupon(BigDecimal orderTotal, String couponCode) throws ServiceException;
    
    /**
     * Calculates discount amount without applying it.
     * Used for preview/display purposes.
     * 
     * @param couponCode the coupon code
     * @param orderTotal the order total
     * @return discount amount that would be applied
     */
    BigDecimal getDiscountAmount(String couponCode, BigDecimal orderTotal);
    
    /**
     * Retrieves all active (non-expired and isActive=true) coupons.
     * 
     * @return List of active coupons
     */
    List<Coupon> getActiveCoupons();
    
    /**
     * Checks if a coupon code already exists.
     * 
     * @param couponCode the code to check
     * @return true if code exists, false otherwise
     */
    boolean isCouponCodeDuplicate(String couponCode);
    
    /**
     * Deactivates a coupon (sets isActive to false).
     * 
     * @param couponCode the coupon code
     * @return true if deactivation successful
     */
    boolean deactivateCoupon(String couponCode);
}
