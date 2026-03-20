package com.oop.project.service.impl;

import com.oop.project.service.interfaces.BillingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of BillingService interface.
 * Demonstrates method overloading with 3 computeBill methods.
 * 
 * @author Service Team
 * @version 1.0
 */
public class BillingServiceImpl implements BillingService {
    
    /**
     * OVERLOADED METHOD #1
     * Computes bill for a single item price with tax.
     * Formula: price × (1 + TAX_RATE)
     * 
     * @param price the item price
     * @return final total with 8% tax applied
     */
    @Override
    public BigDecimal computeBill(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        return applyTax(price);
    }
    
    /**
     * OVERLOADED METHOD #2
     * Computes bill for price and quantity with tax.
     * Formula: (price × quantity) × (1 + TAX_RATE)
     * 
     * @param price the item price
     * @param quantity the quantity ordered
     * @return final total with 8% tax applied
     */
    @Override
    public BigDecimal computeBill(BigDecimal price, int quantity) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        BigDecimal subtotal = calculateSubtotal(price, quantity);
        return applyTax(subtotal);
    }
    
    /**
     * OVERLOADED METHOD #3
     * Computes full bill with price, quantity, coupon discount, and tax.
     * Formula: ((price × quantity) - discount) × (1 + TAX_RATE)
     * 
     * @param price the item price
     * @param quantity the quantity ordered
     * @param couponDiscount the discount amount from coupon
     * @return final total with discount and tax applied
     */
    @Override
    public BigDecimal computeBill(BigDecimal price, int quantity, BigDecimal couponDiscount) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (couponDiscount == null) {
            couponDiscount = BigDecimal.ZERO;
        }
        if (couponDiscount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }
        
        // Step 1: Calculate subtotal
        BigDecimal subtotal = calculateSubtotal(price, quantity);
        
        // Step 2: Apply discount
        BigDecimal afterDiscount = subtotal.subtract(couponDiscount);
        
        // Ensure total doesn't go negative
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            afterDiscount = BigDecimal.ZERO;
        }
        
        // Step 3: Apply tax
        return applyTax(afterDiscount);
    }
    
    /**
     * Applies tax to a given amount.
     * Formula: amount × (1 + TAX_RATE) = amount × 1.08
     * 
     * @param amount the pre-tax amount
     * @return amount with 8% tax added
     */
    @Override
    public BigDecimal applyTax(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        
        // Multiply by (1 + 0.08) = 1.08
        BigDecimal multiplier = BigDecimal.ONE.add(TAX_RATE);
        return amount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates subtotal before tax and discount.
     * Formula: price × quantity
     * 
     * @param price the item price
     * @param quantity the quantity
     * @return subtotal amount
     */
    @Override
    public BigDecimal calculateSubtotal(BigDecimal price, int quantity) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates tax amount only (not including original amount).
     * Formula: amount × TAX_RATE = amount × 0.08
     * 
     * @param amount the pre-tax amount
     * @return tax amount only
     */
    @Override
    public BigDecimal calculateTaxAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Generates detailed billing breakdown for display.
     * 
     * Example output:
     * {
     *   "subtotal": 100.00,
     *   "discount": 10.00,
     *   "taxAmount": 7.20,
     *   "finalTotal": 97.20
     * }
     * 
     * @param price the item price
     * @param quantity the quantity
     * @param couponDiscount the discount amount
     * @return Map with billing breakdown
     */
    @Override
    public Map<String, BigDecimal> generateBillingBreakdown(BigDecimal price, int quantity, 
                                                             BigDecimal couponDiscount) {
        Map<String, BigDecimal> breakdown = new HashMap<>();
        
        if (couponDiscount == null) {
            couponDiscount = BigDecimal.ZERO;
        }
        
        // Calculate each component
        BigDecimal subtotal = calculateSubtotal(price, quantity);
        BigDecimal afterDiscount = subtotal.subtract(couponDiscount);
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            afterDiscount = BigDecimal.ZERO;
        }
        
        BigDecimal taxAmount = calculateTaxAmount(afterDiscount);
        BigDecimal finalTotal = afterDiscount.add(taxAmount);
        
        // Populate breakdown
        breakdown.put("subtotal", subtotal);
        breakdown.put("discount", couponDiscount);
        breakdown.put("taxAmount", taxAmount);
        breakdown.put("finalTotal", finalTotal);
        
        return breakdown;
    }
}
