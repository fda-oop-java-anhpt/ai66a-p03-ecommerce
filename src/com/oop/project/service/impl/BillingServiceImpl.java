package com.oop.project.service.impl;

import com.oop.project.model.Coupon;
import com.oop.project.model.DiscountType;
import com.oop.project.model.Order;
import com.oop.project.model.OrderDetail;
import com.oop.project.service.interfaces.BillingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Implementation of BillingService.
 *
 * FR-3.3: THREE overloaded computeBill() methods (EXTREMELY IMPORTANT)
 * FR-3.4: Apply fixed 8% tax to every order
 * FR-3.5: Generate printable invoice
 *
 * TAX RATE: 8% (0.08) — all calculations apply this tax.
 *
 * METHOD OVERLOADING SUMMARY:
 *  computeBill(price)                         → price × 1.08
 *  computeBill(price, quantity)               → (price × qty) × 1.08
 *  computeBill(price, quantity, discount)     → (price × qty − discount) × 1.08
 *
 * @author Lan - Service Layer
 */
public class BillingServiceImpl implements BillingService {

    // ── Tax rate constant (FR-3.4) ────────────────────────────────
    private static final BigDecimal TAX_RATE          = new BigDecimal("0.08");
    private static final BigDecimal TAX_MULTIPLIER    = new BigDecimal("1.08");
    private static final int        SCALE             = 2;
    private static final RoundingMode ROUNDING        = RoundingMode.HALF_UP;

    // ─────────────────────────────────────────────────────────────
    // FR-3.3: OVERLOAD 1 — computeBill(price)
    // ─────────────────────────────────────────────────────────────

    /**
     * Compute bill for a single unit with no discount.
     *
     * Formula: total = price × 1.08
     */
    @Override
    public BigDecimal computeBill(BigDecimal price) {
        validatePrice(price);
        return applyTax(price).setScale(SCALE, ROUNDING);
    }

    // ─────────────────────────────────────────────────────────────
    // FR-3.3: OVERLOAD 2 — computeBill(price, quantity)
    // ─────────────────────────────────────────────────────────────

    /**
     * Compute bill for multiple units with no discount.
     *
     * Formula: total = (price × quantity) × 1.08
     */
    @Override
    public BigDecimal computeBill(BigDecimal price, int quantity) {
        validatePrice(price);
        validateQuantity(quantity);

        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
        return applyTax(subtotal).setScale(SCALE, ROUNDING);
    }

    // ─────────────────────────────────────────────────────────────
    // FR-3.3: OVERLOAD 3 — computeBill(price, quantity, couponDiscount)
    // ─────────────────────────────────────────────────────────────

    /**
     * Compute bill with quantity and coupon discount — FULL calculation.
     *
     * Formula:
     *   subtotal      = price × quantity
     *   afterDiscount = subtotal − couponDiscount  (min: 0, cannot go negative)
     *   total         = afterDiscount × 1.08
     */
    @Override
    public BigDecimal computeBill(BigDecimal price, int quantity, BigDecimal couponDiscount) {
        validatePrice(price);
        validateQuantity(quantity);
        if (couponDiscount == null || couponDiscount.compareTo(BigDecimal.ZERO) < 0) {
            couponDiscount = BigDecimal.ZERO;
        }

        BigDecimal subtotal      = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal afterDiscount = subtotal.subtract(couponDiscount);

        // Prevent negative amount (discount cannot exceed subtotal)
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            afterDiscount = BigDecimal.ZERO;
        }

        return applyTax(afterDiscount).setScale(SCALE, ROUNDING);
    }

    // ─────────────────────────────────────────────────────────────
    // FULL ORDER BILLING
    // ─────────────────────────────────────────────────────────────

    /**
     * Calculate complete billing for an Order.
     *
     * Steps:
     *  1. Sum all OrderDetail.priceAtTime × quantity → subtotal
     *  2. Apply coupon discount if present
     *  3. Apply 8% tax to afterDiscount → finalTotal
     *  4. Set Order fields: subtotal, discountAmount, discountInfo, finalTotal
     */
    @Override
    public Order calculateOrderBilling(Order order) {
        if (order == null || order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item.");
        }

        // Step 1: Calculate subtotal from all order items
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDetail detail : order.getOrderItems()) {
            BigDecimal lineTotal = detail.getPriceAtTime()
                .multiply(BigDecimal.valueOf(detail.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }
        order.setSubtotal(subtotal);

        // Step 2: Apply coupon discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        String discountInfo = "No discount";

        Coupon coupon = order.getCoupon();
        if (coupon != null) {
            discountAmount = calculateCouponDiscount(coupon, subtotal);
            discountInfo   = buildDiscountInfo(coupon, discountAmount);
        }
        order.setDiscountAmount(discountAmount);
        order.setDiscountInfo(discountInfo);

        // Step 3: Apply 8% tax
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) afterDiscount = BigDecimal.ZERO;

        BigDecimal finalTotal = applyTax(afterDiscount).setScale(SCALE, ROUNDING);
        order.setFinalTotal(finalTotal);

        return order;
    }

    // ─────────────────────────────────────────────────────────────
    // FR-3.5: INVOICE GENERATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Generate a formatted invoice string for display or printing.
     *
     * Sample output:
     * ═══════════════════════════════════
     *            INVOICE
     * ═══════════════════════════════════
     * Order ID  : ORD-001
     * Customer  : Nguyen Van A
     * Date      : 2026-03-24
     * ───────────────────────────────────
     * Item                  Qty    Price
     * ───────────────────────────────────
     * Shirt SHIRT-001         2   50.00
     * ───────────────────────────────────
     * Subtotal               :   100.00
     * Discount (SAVE10 10%) :   -10.00
     * Tax (8%)               :     7.20
     * ═══════════════════════════════════
     * TOTAL                  :    97.20
     * ═══════════════════════════════════
     */
    @Override
    public String generateInvoice(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null.");

        NumberFormat currency = NumberFormat.getNumberInstance(Locale.US);
        currency.setMinimumFractionDigits(2);
        currency.setMaximumFractionDigits(2);

        StringBuilder sb = new StringBuilder();
        String line  = "─".repeat(45);
        String dline = "═".repeat(45);

        sb.append(dline).append("\n");
        sb.append(center("INVOICE", 45)).append("\n");
        sb.append(dline).append("\n");

        sb.append(String.format("%-15s: %s%n", "Order ID",
            "ORD-" + String.format("%03d", order.getOrderId())));
        sb.append(String.format("%-15s: %s%n", "Customer",
            order.getCustomer() != null ? order.getCustomer().getCustomerName() : "Unknown"));
        sb.append(String.format("%-15s: %s%n", "Date",
            order.getOrderDate() != null ? order.getOrderDate().toString().substring(0, 10) : "N/A"));
        sb.append(String.format("%-15s: %s%n", "Status",
            order.getStatus() != null ? order.getStatus().name() : "N/A"));

        sb.append(line).append("\n");
        sb.append(String.format("%-25s %5s %12s%n", "Item", "Qty", "Price"));
        sb.append(line).append("\n");

        // List all order items
        if (order.getOrderItems() != null) {
            for (OrderDetail detail : order.getOrderItems()) {
                String itemName = (detail.getItem() != null)
                    ? detail.getItem().getItemName()
                    : "Unknown Item";
                sb.append(String.format("%-25s %5d %12s%n",
                    truncate(itemName, 24),
                    detail.getQuantity(),
                    currency.format(detail.getPriceAtTime()
                        .multiply(BigDecimal.valueOf(detail.getQuantity())))));
            }
        }

        sb.append(line).append("\n");

        // Subtotal
        BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
        sb.append(String.format("%-30s %12s%n", "Subtotal", "$" + currency.format(subtotal)));

        // Discount
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        String discountLabel = order.getDiscountInfo() != null ? order.getDiscountInfo() : "No discount";
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s %12s%n",
                "Discount (" + truncate(discountLabel, 18) + ")",
                "-$" + currency.format(discount)));
        }

        // Tax
        BigDecimal afterDiscount = subtotal.subtract(discount);
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) afterDiscount = BigDecimal.ZERO;
        BigDecimal taxAmount = afterDiscount.multiply(TAX_RATE).setScale(SCALE, ROUNDING);
        sb.append(String.format("%-30s %12s%n", "Tax (8%)", "+$" + currency.format(taxAmount)));

        sb.append(dline).append("\n");

        // Final total
        BigDecimal finalTotal = order.getFinalTotal() != null ? order.getFinalTotal() : BigDecimal.ZERO;
        sb.append(String.format("%-30s %12s%n", "TOTAL", "$" + currency.format(finalTotal)));

        sb.append(dline).append("\n");
        sb.append(center("Thank you for your purchase!", 45)).append("\n");
        sb.append(dline);

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────
    // FR-3.4: APPLY TAX
    // ─────────────────────────────────────────────────────────────

    /**
     * Apply 8% tax: result = amount × 1.08
     */
    @Override
    public BigDecimal applyTax(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(TAX_MULTIPLIER).setScale(SCALE, ROUNDING);
    }

    @Override
    public BigDecimal getTaxRate() {
        return TAX_RATE.multiply(new BigDecimal("100")); // Returns 8.00
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private BigDecimal calculateCouponDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getDiscountType() == DiscountType.Percent) {
            // Percentage discount
            return subtotal.multiply(coupon.getDiscountValue()
                .divide(new BigDecimal("100"), 4, ROUNDING))
                .setScale(SCALE, ROUNDING);
        } else {
            // Fixed discount (capped at subtotal)
            BigDecimal fixed = coupon.getDiscountValue();
            return fixed.compareTo(subtotal) > 0 ? subtotal : fixed;
        }
    }

    private String buildDiscountInfo(Coupon coupon, BigDecimal discountAmount) {
        if (coupon.getDiscountType() == DiscountType.Percent) {
            return coupon.getCouponCode() + " " + coupon.getDiscountValue() + "% off";
        } else {
            return coupon.getCouponCode() + " $" + coupon.getDiscountValue() + " off";
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
    }

    private String center(String text, int width) {
        int pad = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, pad)) + text;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 1) + "…" : text;
    }
}