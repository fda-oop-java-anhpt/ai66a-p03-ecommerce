package com.oop.project.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class Order {
    private int orderId;
    
    // TỐI ƯU OOP: Thay thế customerId và couponCode bằng Object
    private Customer customer; 
    private Coupon coupon;     
    
    // TỐI ƯU OOP: Đơn hàng tự quản lý danh sách các món hàng bên trong nó
    private List<OrderDetail> orderDetail; 

    private BigDecimal taxRate;
    private BigDecimal discountAmount;
    private String discountInfo;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal finalTotal;
    private Timestamp orderDate;

    public Order() {
        // Luôn khởi tạo danh sách rỗng để tránh lỗi NullPointerException khi gọi .add()
        this.orderDetail = new ArrayList<>();
        this.taxRate = new BigDecimal("8.00"); // Mặc định theo FR-3.4
        this.discountAmount = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.finalTotal = BigDecimal.ZERO;
    }

    public Order(int orderId, Customer customer, Coupon coupon, BigDecimal taxRate, 
                 BigDecimal discountAmount, String discountInfo, OrderStatus status, 
                 BigDecimal subtotal, BigDecimal finalTotal, Timestamp orderDate) {
        this.orderId = orderId;
        this.customer = customer;
        this.coupon = coupon;
        this.orderDetail = new ArrayList<>();
        setTaxRate(taxRate);
        setDiscountAmount(discountAmount);
        this.discountInfo = discountInfo;
        this.status = status;
        setSubtotal(subtotal);
        setFinalTotal(finalTotal);
        this.orderDate = orderDate;
    }

    // --- GETTER & SETTER ---

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { 
        if (customer == null) throw new IllegalArgumentException("Customer cannot be null !");
        this.customer = customer; 
    }

    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }

    public List<OrderDetail> getOrderItems() { return orderDetail; }
    public void setOrderItems(List<OrderDetail> orderDetail) { this.orderDetail = orderDetail; }

    // TỐI ƯU OOP: Hàm phụ trợ để thêm từng món hàng vào đơn dễ dàng hơn
    public void addOrderItem(OrderDetail item) {
        if (item != null) this.orderDetail.add(item);
    }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { 
        if (taxRate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Tax rate cannot be negative!");
        this.taxRate = taxRate; 
    }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) {
        // Kiểm tra điều kiện CHECK (discount_amount >= 0) từ database
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Discount amount cannot be negative!");
        this.discountAmount = discountAmount;
    }

    public String getDiscountInfo() { return discountInfo; }
    public void setDiscountInfo(String discountInfo) { this.discountInfo = discountInfo; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) {
        // Kiểm tra điều kiện CHECK (subtotal > 0)
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0){
        throw new IllegalArgumentException("Subtotal must be greater than 0!");
        } 
        this.subtotal = subtotal;
    }

    public BigDecimal getFinalTotal() { return finalTotal; }
    public void setFinalTotal(BigDecimal finalTotal) {
        if (finalTotal.compareTo(BigDecimal.ZERO) <= 0){ 
            throw new IllegalArgumentException("Final total must be greater than 0 !");
        }
        this.finalTotal = finalTotal;
    }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    @Override
    public String toString() {
        // Tránh in toàn bộ object Customer ra gây rối mắt, chỉ in tên
        String cusName = (customer != null) ? customer.getCustomerName() : "Unknown";
        return "Order{id=" + orderId + ", customer=" + cusName + ", total=" + finalTotal + ", items=" + orderDetail.size() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId;
    }
}