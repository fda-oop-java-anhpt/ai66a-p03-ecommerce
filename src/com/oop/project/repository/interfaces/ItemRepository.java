package com.oop.project.repository.interfaces;

import java.util.List;

import com.oop.project.model.Item;

public interface ItemRepository {
    List<Item> findAll();
    Item findBySku(String sku);
    boolean isSkuExists(String sku);
    boolean insert(Item item);
    boolean update(Item item);
    boolean delete(String sku);
    boolean updateStock(String sku, int quantityChange);
}