package com.oop.project.service.interfaces;

import java.math.BigDecimal;

/**
 * Service interface for billing calculations.
 * ⭐ CỰC KỲ QUAN TRỌNG - Core requirement of the project.
 * 
 * Contains 3 overloaded computeBill() methods demonstrating method overloading:
 * 1. computeBill(price) - Price only with tax
 * 2. computeBill(price, quantity) - Price × Quantity with tax
 * 3. computeBill(price, quantity, discount) - Full calculation with coupon and tax
 * 
 * @author Service Team
 * @version 1.0
 */
public interface BillingService {
    
    /**
     * TAX RATE constant - 8% tax applied to all calculations.
     * This rate is used in all computeBill methods.
     */
    public static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    
    /**
     * OVERLOADED METHOD #1
     * Computes bill for a single item price with tax.
     * Formula: price × (1 + TAX_RATE)
     * 
     * Example: computeBill(100.00) → 108.00
     * 
     * @param price the item price
     * @return final total with 8% tax applied
     */
    BigDecimal computeBill(BigDecimal price);
    
    /**
     * OVERLOADED METHOD #2
     * Computes bill for price and quantity with tax.
     * Formula: (price × quantity) × (1 + TAX_RATE)
     * 
     * Example: computeBill(50.00, 2) → 108.00
     * 
     * @param price the item price
     * @param quantity the quantity ordered
     * @return final total with 8% tax applied
     */
    BigDecimal computeBill(BigDecimal price, int quantity);
    
    /**
     * OVERLOADED METHOD #3
     * Computes full bill with price, quantity, coupon discount, and tax.
     * Formula: ((price × quantity) - discount) × (1 + TAX_RATE)
     * 
     * Example: computeBill(50.00, 2, 10.00) → 97.20
     * Breakdown:
     *   - Subtotal: 50.00 × 2 = 100.00
     *   - After discount: 100.00 - 10.00 = 90.00
     *   - Tax: 90.00 × 0.08 = 7.20
     *   - Final: 90.00 + 7.20 = 97.20
     * 
     * @param price the item price
     * @param quantity the quantity ordered
     * @param couponDiscount the discount amount from coupon
     * @return final total with discount and tax applied
     */
    BigDecimal computeBill(BigDecimal price, int quantity, BigDecimal couponDiscount);
    
    /**
     * Applies tax to a given amount.
     * Helper method used by all overloaded computeBill methods.
     * Formula: amount × (1 + TAX_RATE)
     * 
     * @param amount the pre-tax amount
     * @return amount with 8% tax added
     */
    BigDecimal applyTax(BigDecimal amount);
    
    /**
     * Calculates subtotal before tax and discount.
     * Formula: price × quantity
     * 
     * @param price the item price
     * @param quantity the quantity
     * @return subtotal amount
     */
    BigDecimal calculateSubtotal(BigDecimal price, int quantity);
    
    /**
     * Calculates tax amount only (not including original amount).
     * Formula: amount × TAX_RATE
     * 
     * @param amount the pre-tax amount
     * @return tax amount only
     */
    BigDecimal calculateTaxAmount(BigDecimal amount);
    
    /**
     * Generates detailed billing breakdown for display.
     * Used by UI to show itemized calculations.
     * 
     * @param price the item price
     * @param quantity the quantity
     * @param couponDiscount the discount amount
     * @return Map with keys: "subtotal", "discount", "taxAmount", "finalTotal"
     */
    java.util.Map<String, BigDecimal> generateBillingBreakdown(BigDecimal price, int quantity, BigDecimal couponDiscount);
}
