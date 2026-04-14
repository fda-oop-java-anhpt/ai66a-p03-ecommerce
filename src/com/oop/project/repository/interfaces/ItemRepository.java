package com.oop.project.repository.interfaces;

import com.oop.project.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> findAll();

    Item findBySku(String sku);

    boolean isSkuExists(String sku);

    boolean insert(Item item);

    boolean update(Item item);

    boolean delete(String sku);

    boolean updateStock(String sku, int quantityChange);

    boolean hasBeenOrdered(String sku);
}