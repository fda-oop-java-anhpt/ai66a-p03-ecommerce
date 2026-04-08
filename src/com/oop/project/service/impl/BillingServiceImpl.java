package com.oop.project.service.impl;

import com.oop.project.exception.CouponExpiredException;
import com.oop.project.exception.InsufficientStockException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.*;
import com.oop.project.repository.interfaces.*;
import com.oop.project.repository.impl.*;
import com.oop.project.service.interfaces.IBillingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

public class BillingServiceImpl implements IBillingService {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository orderDetailRepo;
    private final ItemRepository itemRepo;
    private final CouponRepository couponRepo;
    private final SystemSettingRepository settingRepo;
    private final AuditLogRepository auditLogRepo;

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("8.00");

    public BillingServiceImpl() {
        this.orderRepo = new OrderRepositoryImpl();
        this.orderDetailRepo = new OrderDetailRepositoryImpl();
        this.itemRepo = new ItemRepositoryImpl();
        this.couponRepo = new CouponRepositoryImpl();
        this.settingRepo = new SystemSettingRepositoryImpl();
        this.auditLogRepo = new AuditLogRepositoryImpl();
    }

    // Constructor for dependency injection
    public BillingServiceImpl(OrderRepository orderRepo, OrderDetailRepository orderDetailRepo,
                              ItemRepository itemRepo, CouponRepository couponRepo,
                              SystemSettingRepository settingRepo, AuditLogRepository auditLogRepo) {
        this.orderRepo = orderRepo;
        this.orderDetailRepo = orderDetailRepo;
        this.itemRepo = itemRepo;
        this.couponRepo = couponRepo;
        this.settingRepo = settingRepo;
        this.auditLogRepo = auditLogRepo;
    }

    @Override
    public BigDecimal computeBill(BigDecimal price) {
        return price != null ? price : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal computeBill(BigDecimal price, int quantity) {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public BigDecimal computeBill(BigDecimal price, int quantity, BigDecimal couponDiscount) {
        BigDecimal lineTotal = computeBill(price, quantity);
        if (couponDiscount == null) return lineTotal;
        BigDecimal result = lineTotal.subtract(couponDiscount);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    @Override
    public Order createOrder(Order order, User currentUser) {
        if (order == null || currentUser == null) {
            throw new ValidationException("Order and current user must not be null.");
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new ValidationException("Order must contain at least one item.");
        }
        if (order.getCustomer() == null) {
            throw new ValidationException("Order must have a customer.");
        }

        // Validate stock
        for (OrderDetail detail : order.getOrderItems()) {
            if (detail.getItem() == null || detail.getItem().getItemSku() == null) continue;
            Item dbItem = itemRepo.findBySku(detail.getItem().getItemSku());
            if (dbItem == null) {
                throw new ValidationException("Item not found: " + detail.getItem().getItemSku());
            }
            if (detail.getQuantity() > dbItem.getStockQuantity()) {
                throw new InsufficientStockException("Insufficient stock for '" + dbItem.getItemName() +
                        "'. Requested: " + detail.getQuantity() + ", Available: " + dbItem.getStockQuantity());
            }
        }

        // Calculate subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDetail detail : order.getOrderItems()) {
            BigDecimal lineTotal = computeBill(detail.getPriceAtTime(), detail.getQuantity());
            subtotal = subtotal.add(lineTotal);
        }
        order.setSubtotal(subtotal);

        // Apply coupon if present
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (order.getCoupon() != null && order.getCoupon().getCouponCode() != null) {
            Coupon coupon = validateCoupon(order.getCoupon().getCouponCode(), subtotal);
            discountAmount = calculateDiscount(coupon, subtotal);
            order.setCoupon(coupon);
            order.setDiscountInfo(coupon.getDiscountType() + ": " + coupon.getDiscountValue());
        }
        order.setDiscountAmount(discountAmount);

        // Tax rate
        BigDecimal taxRate = getTaxRate();
        order.setTaxRate(taxRate);

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) afterDiscount = BigDecimal.ZERO;
        BigDecimal taxAmount = afterDiscount.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = afterDiscount.add(taxAmount);
        order.setFinalTotal(finalTotal);

        order.setStatus(OrderStatus.PENDING);
        if (order.getOrderDate() == null) {
            order.setOrderDate(new Timestamp(System.currentTimeMillis()));
        }

        // Save order
        int orderId = orderRepo.insert(order);
        if (orderId <= 0) throw new RuntimeException("Failed to create order.");
        order.setOrderId(orderId);

        // Save order details
        for (OrderDetail detail : order.getOrderItems()) {
            detail.setOrderId(orderId);
        }
        orderDetailRepo.insertBatch(orderId, order.getOrderItems());

        // Update stock
        for (OrderDetail detail : order.getOrderItems()) {
            if (detail.getItem() != null && detail.getItem().getItemSku() != null) {
                itemRepo.updateStock(detail.getItem().getItemSku(), -detail.getQuantity());
            }
        }

        // Audit log
        logAudit(currentUser, "CREATE_ORDER", "ORDER", String.valueOf(orderId));
        return order;
    }

    @Override
    public Order updateOrder(Order order, User currentUser) {
        if (order == null || currentUser == null) {
            throw new ValidationException("Order and current user must not be null.");
        }
        Order existing = orderRepo.findById(order.getOrderId());
        if (existing == null) {
            throw new ValidationException("Order not found: " + order.getOrderId());
        }

        // Similar logic as create but update instead of insert
        // (simplified: recalc, then update order and details)
        // Recalculate subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDetail detail : order.getOrderItems()) {
            BigDecimal lineTotal = computeBill(detail.getPriceAtTime(), detail.getQuantity());
            subtotal = subtotal.add(lineTotal);
        }
        order.setSubtotal(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (order.getCoupon() != null && order.getCoupon().getCouponCode() != null) {
            Coupon coupon = validateCoupon(order.getCoupon().getCouponCode(), subtotal);
            discountAmount = calculateDiscount(coupon, subtotal);
            order.setCoupon(coupon);
            order.setDiscountInfo(coupon.getDiscountType() + ": " + coupon.getDiscountValue());
        }
        order.setDiscountAmount(discountAmount);

        BigDecimal taxRate = getTaxRate();
        order.setTaxRate(taxRate);

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) afterDiscount = BigDecimal.ZERO;
        BigDecimal taxAmount = afterDiscount.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = afterDiscount.add(taxAmount);
        order.setFinalTotal(finalTotal);

        boolean updated = orderRepo.update(order);
        if (!updated) throw new RuntimeException("Failed to update order.");

        // Replace order details
        orderDetailRepo.deleteByOrderId(order.getOrderId());
        for (OrderDetail detail : order.getOrderItems()) {
            detail.setOrderId(order.getOrderId());
        }
        orderDetailRepo.insertBatch(order.getOrderId(), order.getOrderItems());

        logAudit(currentUser, "UPDATE_ORDER", "ORDER", String.valueOf(order.getOrderId()));
        return order;
    }

    @Override
    public boolean cancelOrder(int orderId, User currentUser) {
        if (currentUser == null) throw new ValidationException("Current user must not be null.");
        Order order = orderRepo.findById(orderId);
        if (order == null) throw new ValidationException("Order not found.");

        boolean cancelled = orderRepo.updateStatus(orderId, OrderStatus.CANCELLED.name());
        if (cancelled && order.getOrderItems() != null) {
            // Restore stock
            for (OrderDetail detail : order.getOrderItems()) {
                if (detail.getItem() != null && detail.getItem().getItemSku() != null) {
                    itemRepo.updateStock(detail.getItem().getItemSku(), detail.getQuantity());
                }
            }
            logAudit(currentUser, "CANCEL_ORDER", "ORDER", String.valueOf(orderId));
        }
        return cancelled;
    }

    @Override
    public String generateInvoice(Order order) {
        if (order == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════════════════════\n");
        sb.append("                  INVOICE                   \n");
        sb.append("════════════════════════════════════════════\n");
        sb.append(String.format("  Order ID   : #%d\n", order.getOrderId()));
        sb.append(String.format("  Date       : %s\n", order.getOrderDate() != null ? order.getOrderDate() : "N/A"));
        if (order.getCustomer() != null) {
            sb.append(String.format("  Customer   : %s\n", order.getCustomer().getCustomerName()));
            sb.append(String.format("  Phone      : %s\n", order.getCustomer().getPhone()));
        }
        sb.append("────────────────────────────────────────────\n");
        sb.append(String.format("  %-20s %5s %10s %10s\n", "Item", "Qty", "Price", "Total"));
        sb.append("────────────────────────────────────────────\n");
        if (order.getOrderItems() != null) {
            for (OrderDetail detail : order.getOrderItems()) {
                String itemName = detail.getItem() != null ? detail.getItem().getItemName() : "Unknown";
                BigDecimal lineTotal = detail.getPriceAtTime().multiply(BigDecimal.valueOf(detail.getQuantity()));
                sb.append(String.format("  %-20s %5d %10s %10s\n",
                        truncate(itemName, 20),
                        detail.getQuantity(),
                        detail.getPriceAtTime().setScale(2, RoundingMode.HALF_UP),
                        lineTotal.setScale(2, RoundingMode.HALF_UP)));
            }
        }
        sb.append("────────────────────────────────────────────\n");
        sb.append(String.format("  Subtotal            : %15s\n", order.getSubtotal().setScale(2, RoundingMode.HALF_UP)));
        if (order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("  Discount (%s) : -%14s\n",
                    order.getDiscountInfo() != null ? order.getDiscountInfo() : "",
                    order.getDiscountAmount().setScale(2, RoundingMode.HALF_UP)));
        }
        BigDecimal afterDiscount = order.getSubtotal().subtract(order.getDiscountAmount());
        BigDecimal taxAmount = afterDiscount.multiply(order.getTaxRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        sb.append(String.format("  Tax (%s%%)          : %15s\n",
                order.getTaxRate().setScale(2, RoundingMode.HALF_UP),
                taxAmount.setScale(2, RoundingMode.HALF_UP)));
        sb.append("════════════════════════════════════════════\n");
        sb.append(String.format("  FINAL TOTAL         : %15s\n", order.getFinalTotal().setScale(2, RoundingMode.HALF_UP)));
        sb.append("════════════════════════════════════════════\n");
        sb.append(String.format("  Status: %s\n", order.getStatus()));
        sb.append("\n  Thank you for your purchase!\n");
        return sb.toString();
    }

    // Private helpers
    private Coupon validateCoupon(String code, BigDecimal orderTotal) {
        Coupon coupon = couponRepo.findByCode(code);
        if (coupon == null) {
            throw new CouponExpiredException("Coupon '" + code + "' not found.");
        }
        if (!coupon.isActive()) {
            throw new CouponExpiredException("Coupon '" + code + "' is no longer active.");
        }
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().before(today)) {
            throw new CouponExpiredException("Coupon '" + code + "' has expired on " + coupon.getExpiryDate());
        }
        if (coupon.getMinOrderValue() != null && orderTotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new CouponExpiredException("Order total must be at least " + coupon.getMinOrderValue() + " to use coupon '" + code + "'.");
        }
        return coupon;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getDiscountType() == DiscountType.Percent) {
            return subtotal.multiply(coupon.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            return coupon.getDiscountValue();
        }
    }

    private BigDecimal getTaxRate() {
        SystemSetting setting = settingRepo.findByKey("TAX_RATE");
        if (setting != null && setting.getSettingValue() != null) {
            try {
                return new BigDecimal(setting.getSettingValue());
            } catch (NumberFormatException ignored) {}
        }
        return DEFAULT_TAX_RATE;
    }

    private void logAudit(User user, String action, String targetType, String targetId) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setActions(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        auditLogRepo.insert(log);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() <= maxLen ? str : str.substring(0, maxLen - 2) + "..";
    }
}