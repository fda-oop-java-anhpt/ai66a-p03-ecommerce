package com.oop.project.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Item {
    private String itemSku;
    private String itemName;
    private String category;
    private BigDecimal unitPrice; // Dùng BigDecimal cho tiền tệ để chính xác tuyệt đối
    private int stockQuantity;

    public Item(String itemSku, String itemName, String category, BigDecimal unitPrice, int stockQuantity) {
        setItemSku(itemSku);
        setItemName(itemName);
        setCategory(category);
        setUnitPrice(unitPrice);
        setStockQuantity(stockQuantity);
    }

    public Item() {
    }

    // Getter và Setter
    public String getItemSku() {
        return itemSku;
    }

    public void setItemSku(String itemSku) {
        if (itemSku == null || itemSku.isBlank()) {
            throw new IllegalArgumentException("Item SKU cannot be null or empty");
        }
        this.itemSku = itemSku;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        this.unitPrice = unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than 0");
        }
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String toString() {
        return "Item{" + "sku=" + itemSku + ", name='" + itemName + "'" + ", price='" + unitPrice + "'" + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Item item = (Item) o;
        return itemSku == item.itemSku;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemSku);
    }
}