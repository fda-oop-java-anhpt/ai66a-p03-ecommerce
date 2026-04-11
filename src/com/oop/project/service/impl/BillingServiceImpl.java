package com.oop.project.service.impl;

import com.oop.project.exception.CouponExpiredException;
import com.oop.project.exception.InsufficientStockException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.*;
import com.oop.project.repository.interfaces.*;
import com.oop.project.service.interfaces.IBillingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

public class BillingServiceImpl implements IBillingService {

    private final OrderRepository orderRepo;
    private final AuditLogRepository auditLogRepo;
    private final SystemSettingRepository settingRepo;
    private final CouponRepository couponRepo;
    private final ItemRepository itemRepo;
    // private final OrderDetailRepository orderDetailRepo;

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("8.00");

    public BillingServiceImpl(OrderRepository orderRepo, AuditLogRepository auditLogRepo,
            SystemSettingRepository settingRepo, CouponRepository couponRepo,
            ItemRepository itemRepo, OrderDetailRepository orderDetailRepo) {
        this.orderRepo = orderRepo;
        this.auditLogRepo = auditLogRepo;
        this.settingRepo = settingRepo;
        this.couponRepo = couponRepo;
        this.itemRepo = itemRepo;
        // this.orderDetailRepo = orderDetailRepo;
    }

    @Override
    public BigDecimal computeBill(BigDecimal price) {
        return price;
    }

    @Override
    public BigDecimal computeBill(BigDecimal price, int quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public BigDecimal computeBill(BigDecimal price, int quantity, BigDecimal couponDiscount) {
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));
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

        validateStock(order.getOrderItems());

        BigDecimal subtotal = calculateSubtotal(order.getOrderItems());
        order.setSubtotal(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (order.getCoupon() != null && order.getCoupon().getCouponCode() != null) {
            Coupon validCoupon = validateCoupon(order.getCoupon().getCouponCode(), subtotal);
            discountAmount = calculateDiscount(validCoupon, subtotal);
            order.setCoupon(validCoupon);
            order.setDiscountInfo(validCoupon.getDiscountType() + ": " + validCoupon.getDiscountValue());
        }
        order.setDiscountAmount(discountAmount);

        BigDecimal taxRate = getTaxRate();
        order.setTaxRate(taxRate);

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0)
            afterDiscount = BigDecimal.ZERO;
        BigDecimal taxAmount = afterDiscount.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = afterDiscount.add(taxAmount);
        order.setFinalTotal(finalTotal);

        order.setStatus(OrderStatus.PENDING);
        if (order.getOrderDate() == null)
            order.setOrderDate(new Timestamp(System.currentTimeMillis()));

        int orderId = orderRepo.insert(order);
        if (orderId <= 0)
            throw new RuntimeException("Failed to persist order.");
        order.setOrderId(orderId);

        // orderDetailRepo.insertBatch(orderId, order.getOrderItems());

        for (OrderDetail detail : order.getOrderItems()) {
            itemRepo.updateStock(detail.getItem().getItemSku(), -detail.getQuantity());
        }

        logAudit(currentUser, "CREATE_ORDER", "ORDER", String.valueOf(orderId));
        return order;
    }

    @Override
    public Order updateOrder(Order order, User currentUser) {
        if (order == null || currentUser == null) {
            throw new ValidationException("Order and current user must not be null.");
        }

        validateStock(order.getOrderItems());

        BigDecimal subtotal = calculateSubtotal(order.getOrderItems());
        order.setSubtotal(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (order.getCoupon() != null && order.getCoupon().getCouponCode() != null) {
            Coupon validCoupon = validateCoupon(order.getCoupon().getCouponCode(), subtotal);
            discountAmount = calculateDiscount(validCoupon, subtotal);
            order.setCoupon(validCoupon);
            order.setDiscountInfo(validCoupon.getDiscountType() + ": " + validCoupon.getDiscountValue());
        }
        order.setDiscountAmount(discountAmount);

        BigDecimal taxRate = getTaxRate();
        order.setTaxRate(taxRate);

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0)
            afterDiscount = BigDecimal.ZERO;
        BigDecimal taxAmount = afterDiscount.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = afterDiscount.add(taxAmount);
        order.setFinalTotal(finalTotal);

        boolean updated = orderRepo.update(order);
        if (!updated)
            throw new RuntimeException("Failed to update order.");
        // orderDetailRepo.deleteByOrderId(order.getOrderId());
        // orderDetailRepo.insertBatch(order.getOrderId(), order.getOrderItems());

        logAudit(currentUser, "UPDATE_ORDER", "ORDER", String.valueOf(order.getOrderId()));
        return order;
    }

    @Override
    public boolean cancelOrder(int orderId, User currentUser) {
        if (currentUser == null)
            throw new ValidationException("Current user must not be null.");
        Order order = orderRepo.findById(orderId);
        if (order == null)
            throw new ValidationException("Order not found.");
        // check if order is already cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return false;
        }
        boolean cancelled = orderRepo.updateStatus(orderId, OrderStatus.CANCELLED.name());
        if (cancelled && order.getOrderItems() != null) {
            for (OrderDetail detail : order.getOrderItems()) {
                itemRepo.updateStock(detail.getItem().getItemSku(), detail.getQuantity());
            }
            logAudit(currentUser, "CANCEL_ORDER", "ORDER", String.valueOf(orderId));
        }
        return cancelled;
    }

    @Override
    public String generateInvoice(Order order) {
        if (order == null)
            return "";

        String storeName = "MY STORE";
        SystemSetting setting = settingRepo.findByKey("STORE_NAME");
        if (setting != null && setting.getSettingValue() != null && !setting.getSettingValue().trim().isEmpty()) {
            storeName = setting.getSettingValue().trim();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================\n");
        sb.append(centerText(storeName, 64)).append("\n");
        sb.append("----------------------------------------------------------------\n");
        sb.append("                            INVOICE                             \n");
        sb.append("================================================================\n");
        sb.append(String.format("  Order ID    : #%d\n", order.getOrderId()));
        sb.append(String.format("  Date        : %s\n",
                order.getOrderDate() != null ? order.getOrderDate().toString() : "N/A"));
        if (order.getCustomer() != null) {
            sb.append(String.format("  Customer    : %s\n", order.getCustomer().getCustomerName()));
            sb.append(String.format("  Phone       : %s\n", order.getCustomer().getPhone()));
        }
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format(" %-22s | %4s | %13s | %14s\n", "Item Name", "Qty", "Price", "Total"));
        sb.append("----------------------------------------------------------------\n");

        if (order.getOrderItems() != null) {
            for (OrderDetail detail : order.getOrderItems()) {
                String itemName = detail.getItem() != null ? detail.getItem().getItemName() : "Unknown";
                BigDecimal lineTotal = detail.getPriceAtTime().multiply(BigDecimal.valueOf(detail.getQuantity()));
                sb.append(String.format(" %-22s | %4d | %13s | %14s\n",
                        truncate(itemName, 22),
                        detail.getQuantity(),
                        String.format("%,.0f VNĐ", detail.getPriceAtTime().doubleValue()),
                        String.format("%,.0f VNĐ", lineTotal.doubleValue())));
            }
        }
        sb.append("================================================================\n");
        sb.append(String.format("  Subtotal            : %38s\n",
                String.format("%,.0f VNĐ", order.getSubtotal().doubleValue())));

        if (order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("  Discount %-10s : %38s\n",
                    order.getDiscountInfo() != null ? "(" + truncate(order.getDiscountInfo(), 8) + ")" : "",
                    String.format("-%,.0f VNĐ", order.getDiscountAmount().doubleValue())));
        }

        BigDecimal afterDiscount = order.getSubtotal().subtract(order.getDiscountAmount());
        BigDecimal taxAmount = afterDiscount.multiply(order.getTaxRate()).divide(new BigDecimal("100"), 2,
                RoundingMode.HALF_UP);

        sb.append(String.format("  Tax (%-5s)         : %38s\n",
                order.getTaxRate().stripTrailingZeros().toPlainString() + "%",
                String.format("+%,.0f VNĐ", taxAmount.doubleValue())));

        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("  FINAL TOTAL         : %38s\n",
                String.format("%,.0f VNĐ", order.getFinalTotal().doubleValue())));
        sb.append("================================================================\n");
        sb.append(String.format("  Status: %s\n", order.getStatus()));
        sb.append("\n                  THANK YOU FOR YOUR PURCHASE!\n");
        return sb.toString();
    }

    private void validateStock(List<OrderDetail> items) {
        if (items == null)
            return;
        for (OrderDetail detail : items) {
            if (detail.getItem() != null && detail.getItem().getItemSku() != null) {
                Item dbItem = itemRepo.findBySku(detail.getItem().getItemSku());
                if (dbItem != null && detail.getQuantity() > dbItem.getStockQuantity()) {
                    throw new InsufficientStockException(
                            "Insufficient stock for '" + dbItem.getItemName() +
                                    "'. Requested: " + detail.getQuantity() +
                                    ", Available: " + dbItem.getStockQuantity() + ".");
                }
            }
        }
    }

    private BigDecimal calculateSubtotal(List<OrderDetail> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDetail detail : items) {
            BigDecimal lineTotal = computeBill(detail.getPriceAtTime(), detail.getQuantity());
            subtotal = subtotal.add(lineTotal);
        }
        return subtotal;
    }

    private Coupon validateCoupon(String code, BigDecimal orderTotal) {
        Coupon coupon = couponRepo.findByCode(code);
        if (coupon == null) {
            throw new ValidationException("Coupon '" + code + "' not found.");
        }
        if (!coupon.isActive()) {
            throw new CouponExpiredException("Coupon '" + code + "' is no longer active.");
        }
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().before(today)) {
            throw new CouponExpiredException("Coupon '" + code + "' has expired on " + coupon.getExpiryDate() + ".");
        }
        if (coupon.getMinOrderValue() != null && orderTotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new CouponExpiredException(
                    "Order total must be at least " + coupon.getMinOrderValue() + " to use coupon '" + code + "'.");
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
            } catch (NumberFormatException ignored) {
            }
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
        if (str == null)
            return "";
        return str.length() <= maxLen ? str : str.substring(0, maxLen - 2) + "..";
    }

    private String centerText(String text, int width) {
        if (text == null)
            text = "";
        if (text.length() >= width)
            return text;
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++)
            sb.append(" ");
        sb.append(text);
        for (int i = 0; i < width - text.length() - padding; i++)
            sb.append(" ");
        return sb.toString();
    }
}