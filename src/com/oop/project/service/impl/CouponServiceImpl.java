package com.oop.project.service.impl;

import com.oop.project.model.Coupon;
import com.oop.project.service.interfaces.CouponService;
import com.oop.project.exception.ServiceException;
import com.oop.project.exception.ValidationException;
import com.oop.project.util.Validator;
// import com.oop.project.repository.CouponRepository; // Uncomment Week 4
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.*;

/**
 * Implementation of CouponService interface.
 * Handles coupon management, validation, and discount calculation.
 * 
 * NOTE: Uses stub data for Week 1-3. Integrate with Repository in Week 4.
 * 
 * @author Service Team - Member 3
 * @version 1.0
 */
public class CouponServiceImpl implements CouponService {
    
    // TODO Week 4: Inject repository via constructor
    // private final CouponRepository couponRepository;
    
    /**
     * Creates a new coupon with validation.
     * 
     * @param coupon the Coupon object to create
     * @return the created Coupon
     * @throws ServiceException if coupon code already exists or validation fails
     */
    @Override
    public Coupon createCoupon(Coupon coupon) throws ServiceException {
        try {
            // Validate coupon data
            validateCouponData(coupon);
            
            // Check for duplicate code
            if (isCouponCodeDuplicate(coupon.getCouponCode())) {
                throw new ValidationException("Coupon Code", 
                    "Coupon code already exists: " + coupon.getCouponCode());
            }
            
            // TODO Week 4: return couponRepository.save(coupon);
            
            // STUB
            return coupon;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to create coupon: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates existing coupon information.
     * 
     * @param coupon the Coupon with updated information
     * @return the updated Coupon
     * @throws ServiceException if coupon not found or validation fails
     */
    @Override
    public Coupon updateCoupon(Coupon coupon) throws ServiceException {
        try {
            // Validate coupon data
            validateCouponData(coupon);
            
            // TODO Week 4: Check if coupon exists
            // if (!couponRepository.existsByCode(coupon.getCouponCode())) {
            //     throw new ServiceException("Coupon not found: " + coupon.getCouponCode());
            // }
            
            // TODO Week 4: return couponRepository.update(coupon);
            
            // STUB
            return coupon;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to update coupon: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes a coupon by code.
     * 
     * @param couponCode the coupon code to delete
     * @return true if deletion successful
     * @throws ServiceException if coupon is in use or not found
     */
    @Override
    public boolean deleteCoupon(String couponCode) throws ServiceException {
        try {
            Validator.validateCouponCode(couponCode);
            
            // TODO Week 4: Check if coupon is used in orders
            // if (orderRepository.existsByCouponCode(couponCode)) {
            //     throw new ServiceException("Cannot delete coupon that is used in orders");
            // }
            
            // TODO Week 4: return couponRepository.deleteByCode(couponCode);
            
            // STUB
            return true;
            
        } catch (Exception e) {
            throw new ServiceException("Failed to delete coupon: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves a coupon by code.
     * 
     * @param couponCode the coupon code
     * @return Coupon object if found, null otherwise
     */
    @Override
    public Coupon getCouponByCode(String couponCode) {
        try {
            // TODO Week 4: return couponRepository.findByCode(couponCode);
            
            // STUB
            return null;
        } catch (Exception e) {
            System.err.println("Error retrieving coupon: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Retrieves all coupons in the system.
     * 
     * @return List of all coupons
     */
    @Override
    public List<Coupon> getAllCoupons() {
        try {
            // TODO Week 4: return couponRepository.findAll();
            
            // STUB
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error retrieving coupons: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Validates if a coupon is usable.
     * Checks expiry, active status, and minimum order requirement.
     * 
     * @param couponCode the coupon code to validate
     * @param orderTotal the current order total
     * @return true if coupon is valid
     * @throws ServiceException if coupon is invalid
     */
    @Override
    public boolean validateCoupon(String couponCode, BigDecimal orderTotal) throws ServiceException {
        try {
            // Get coupon
            // TODO Week 4: Coupon coupon = couponRepository.findByCode(couponCode);
            Coupon coupon = getCouponByCode(couponCode);
            
            if (coupon == null) {
                throw new ServiceException("Coupon not found: " + couponCode);
            }
            
            // Check if active
            if (!coupon.isActive()) {
                throw new ServiceException("Coupon is not active");
            }
            
            // Check expiry date
            Date today = new Date(System.currentTimeMillis());
            if (coupon.getExpiryDate().before(today)) {
                throw new ServiceException("Coupon has expired");
            }
            
            // Check minimum order value
            if (orderTotal.compareTo(coupon.getMinOrderValue()) < 0) {
                throw new ServiceException("Order total must be at least " + 
                    coupon.getMinOrderValue() + " to use this coupon");
            }
            
            return true;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Coupon validation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Applies a coupon to an order total and calculates discount amount.
     * Supports "Percent" and "Fixed" discount types.
     * 
     * @param orderTotal the order subtotal before discount
     * @param couponCode the coupon code to apply
     * @return the discount amount to subtract from order total
     * @throws ServiceException if coupon is invalid or cannot be applied
     */
    @Override
    public BigDecimal applyCoupon(BigDecimal orderTotal, String couponCode) throws ServiceException {
        try {
            // Validate coupon
            validateCoupon(couponCode, orderTotal);
            
            // Get coupon
            // TODO Week 4: Coupon coupon = couponRepository.findByCode(couponCode);
            Coupon coupon = getCouponByCode(couponCode);
            
            if (coupon == null) {
                throw new ServiceException("Coupon not found: " + couponCode);
            }
            
            return calculateDiscount(coupon, orderTotal);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to apply coupon: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculates discount amount without applying it.
     * 
     * @param couponCode the coupon code
     * @param orderTotal the order total
     * @return discount amount that would be applied
     */
    @Override
    public BigDecimal getDiscountAmount(String couponCode, BigDecimal orderTotal) {
        try {
            // TODO Week 4: Coupon coupon = couponRepository.findByCode(couponCode);
            Coupon coupon = getCouponByCode(couponCode);
            
            if (coupon == null || !coupon.isActive()) {
                return BigDecimal.ZERO;
            }
            
            // Check expiry
            Date today = new Date(System.currentTimeMillis());
            if (coupon.getExpiryDate().before(today)) {
                return BigDecimal.ZERO;
            }
            
            // Check minimum order
            if (orderTotal.compareTo(coupon.getMinOrderValue()) < 0) {
                return BigDecimal.ZERO;
            }
            
            return calculateDiscount(coupon, orderTotal);
            
        } catch (Exception e) {
            System.err.println("Error calculating discount: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Retrieves all active (non-expired and isActive=true) coupons.
     * 
     * @return List of active coupons
     */
    @Override
    public List<Coupon> getActiveCoupons() {
        try {
            // TODO Week 4: return couponRepository.findActiveCoupons();
            
            // STUB
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error retrieving active coupons: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Checks if a coupon code already exists.
     * 
     * @param couponCode the code to check
     * @return true if code exists, false otherwise
     */
    @Override
    public boolean isCouponCodeDuplicate(String couponCode) {
        try {
            // TODO Week 4: return couponRepository.existsByCode(couponCode);
            
            // STUB
            return false;
        } catch (Exception e) {
            System.err.println("Error checking coupon code: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deactivates a coupon (sets isActive to false).
     * 
     * @param couponCode the coupon code
     * @return true if deactivation successful
     */
    @Override
    public boolean deactivateCoupon(String couponCode) {
        try {
            // TODO Week 4: return couponRepository.deactivate(couponCode);
            
            // STUB
            return true;
        } catch (Exception e) {
            System.err.println("Error deactivating coupon: " + e.getMessage());
            return false;
        }
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    /**
     * Validates coupon data before create/update operations.
     * 
     * @param coupon the coupon to validate
     * @throws ValidationException if validation fails
     */
    private void validateCouponData(Coupon coupon) throws ValidationException {
        if (coupon == null) {
            throw new ValidationException("Coupon object cannot be null");
        }
        
        // Validate coupon code
        Validator.validateCouponCode(coupon.getCouponCode());
        
        // Validate discount value
        Validator.validatePrice(coupon.getDiscountValue(), "Discount Value");
        
        // Validate discount type
        Validator.validateDiscountType(coupon.getDiscountType());
        
        // Validate minimum order value
        if (coupon.getMinOrderValue() != null) {
            Validator.validatePrice(coupon.getMinOrderValue(), "Minimum Order Value");
        }
        
        // Validate expiry date
        if (coupon.getExpiryDate() != null) {
            Validator.validateFutureDate(coupon.getExpiryDate(), "Expiry Date");
        }
        
        // Validate percent discount is not over 100
        if ("Percent".equalsIgnoreCase(coupon.getDiscountType())) {
            if (coupon.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
                throw new ValidationException("Discount Value", 
                    "Percent discount cannot exceed 100%");
            }
        }
    }
    
    /**
     * Calculates discount amount based on coupon type.
     * 
     * @param coupon the coupon
     * @param orderTotal the order total
     * @return discount amount
     */
    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
        if (coupon == null || orderTotal == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount;
        
        if ("Percent".equalsIgnoreCase(coupon.getDiscountType())) {
            // Percent discount: orderTotal × (discountValue / 100)
            discount = orderTotal.multiply(coupon.getDiscountValue())
                               .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            // Fixed discount: flat amount
            discount = coupon.getDiscountValue();
        }
        
        // Ensure discount doesn't exceed order total
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}
