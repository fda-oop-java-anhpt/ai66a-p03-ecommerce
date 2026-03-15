package com.oop.project.model;

import java.math.BigDecimal;

public class Item {
    private String itemSku;
    private String itemName;
    private String category;
    private BigDecimal unitPrice; // Dùng BigDecimal cho tiền tệ để chính xác tuyệt đối
    private int stockQuantity;

    public Item(String itemSku, String itemName, String category, BigDecimal unitPrice, int stockQuantity) {
        this.itemSku = itemSku;
        this.itemName = itemName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
    }
     @Override
    public String toString() {
        return "Item{" +
                "itemSku='" + itemSku + '\'' +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", unitPrice=" + unitPrice +
                ", stockQuantity=" + stockQuantity +
                '}';
    }

    // Getter và Setter
    public String getItemSku() { return itemSku; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getStockQuantity() { return stockQuantity; }
}