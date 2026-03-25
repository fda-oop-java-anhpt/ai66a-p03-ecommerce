package com.oop.project.service.interfaces;

import com.oop.project.model.Order;

import java.math.BigDecimal;

/**
 * FR-3.3: Overloaded computeBill() Methods (EXTREMELY IMPORTANT)
 * FR-3.4: Apply fixed 8% tax to every order
 * FR-3.5: Generate printable invoice
 *
 * This is the core billing engine of the system.
 * Contains THREE overloaded computeBill() methods as required by FR-3.3:
 *
 *   computeBill(price)
 *   computeBill(price, quantity)
 *   computeBill(price, quantity, couponDiscount)
 *
 * All three methods apply 8% tax automatically (FR-3.4).
 *
 * TAX FORMULA: finalTotal = (subtotal - discount) * 1.08
 *
 * @author Lan - Service Layer
 */
public interface BillingService {

    // ─────────────────────────────────────────────────────────────
    // FR-3.3: THREE OVERLOADED computeBill() METHODS
    // ─────────────────────────────────────────────────────────────

    /**
     * [Overload 1] Compute bill for a single unit — no discount applied.
     *
     * Formula: total = price * 1.08 (8% tax)
     *
     * @param price  the unit price of the item (must be > 0)
     * @return the final amount after 8% tax
     */
    BigDecimal computeBill(BigDecimal price);

    /**
     * [Overload 2] Compute bill for multiple units of the same item — no discount.
     *
     * Formula: total = (price × quantity) * 1.08 (8% tax)
     *
     * @param price     the unit price of the item (must be > 0)
     * @param quantity  the number of units (must be >= 1)
     * @return the final amount after tax
     */
    BigDecimal computeBill(BigDecimal price, int quantity);

    /**
     * [Overload 3] Compute bill with quantity AND coupon discount — FULL calculation.
     *
     * Formula: subtotal = price × quantity
     *          afterDiscount = subtotal - couponDiscount
     *          total = afterDiscount * 1.08 (8% tax)
     *
     * FR-3.3: computeBill(price, quantity, couponDiscount)
     *
     * @param price           the unit price of the item (must be > 0)
     * @param quantity        the number of units (must be >= 1)
     * @param couponDiscount  the discount amount already calculated (>= 0)
     * @return the final amount after discount and 8% tax
     */
    BigDecimal computeBill(BigDecimal price, int quantity, BigDecimal couponDiscount);

    // ─────────────────────────────────────────────────────────────
    // FULL ORDER BILLING
    // ─────────────────────────────────────────────────────────────

    /**
     * Calculate the complete billing for an Order object.
     *
     * Steps:
     *  1. Sum all OrderDetail (priceAtTime × quantity) → subtotal
     *  2. Apply coupon discount if Order.getCoupon() != null → discountAmount
     *  3. Apply 8% tax to (subtotal - discount) → finalTotal
     *  4. Set Order.subtotal, Order.discountAmount, Order.finalTotal
     *
     * @param order  the Order with orderDetails and optionally a coupon attached
     * @return the same Order object with subtotal, discountAmount, finalTotal populated
     */
    Order calculateOrderBilling(Order order);

    // ─────────────────────────────────────────────────────────────
    // INVOICE GENERATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Generate a formatted invoice string for an order.
     *
     * FR-3.5: The system shall generate a printable invoice containing:
     *   - Order ID, Customer name, Date
     *   - Line items (Item name, Qty, Price)
     *   - Subtotal, Discount, Tax (8%), Final Total
     *
     * @param order  the Order with all details calculated
     * @return a formatted multi-line String ready to display or print
     */
    String generateInvoice(Order order);

    // ─────────────────────────────────────────────────────────────
    // TAX HELPER
    // ─────────────────────────────────────────────────────────────

    /**
     * Apply 8% tax to a given amount.
     *
     * FR-3.4: The system shall apply a fixed 8% tax to every order.
     *
     * Formula: result = amount * 1.08
     *
     * @param amount  the pre-tax amount (must be >= 0)
     * @return the amount after 8% tax is applied
     */
    BigDecimal applyTax(BigDecimal amount);

    /**
     * Get the current tax rate used by the system.
     *
     * @return the tax rate as a percentage (default: 8.00)
     */
    BigDecimal getTaxRate();
}
