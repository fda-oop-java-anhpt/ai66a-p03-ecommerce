package com.oop.project.model;

import java.math.BigDecimal;

public class OrderDetail {
    private int orderDetailId;
    private int orderId; // Vẫn giữ lại để biết nó thuộc đơn nào khi lưu DB
    
    // TỐI ƯU OOP: Trực tiếp chứa Object Item thay vì lưu SKU
    private Item item; 
    
    private int quantity;
    private BigDecimal priceAtTime;

    public OrderDetail() {}

    public OrderDetail(int orderDetailId, int orderId, Item item, int quantity, BigDecimal priceAtTime) {
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.item = item;
        setQuantity(quantity);
        setPriceAtTime(priceAtTime);
    }

    public int getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(int orderDetailId) { this.orderDetailId = orderDetailId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public Item getItem() { return item; }
    public void setItem(Item item) { 
        if (item == null) throw new IllegalArgumentException("Item cannot be null!");
        this.item = item; 
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        // Kiểm tra điều kiện CHECK (quantity > 0)
        if (quantity <= 0) throw new IllegalArgumentException("The quantity must be greater than 0!");
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtTime() { return priceAtTime; }
    public void setPriceAtTime(BigDecimal priceAtTime) {
        // Kiểm tra điều kiện CHECK (price_at_time > 0)
        if (priceAtTime.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Price at time must be greater than 0!");
        this.priceAtTime = priceAtTime;
    }

    @Override
    public String toString() {
        String itemName = (item != null) ? item.getItemName() : "Unknown";
        return "OrderItem{" + "detailId=" + orderDetailId + ", item=" + itemName + ", quantity=" + quantity + ", price=" + priceAtTime + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetail orderDetail = (OrderDetail) o;
        return orderDetailId == orderDetail.orderDetailId;
    }
}