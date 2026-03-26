package com.oop.project.service;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Item;

public interface ItemService {
    List<Item> getAllItems();
    Optional<Item> getItemBySku(String sku);
    boolean createItem(Item item);
    boolean updateItem(Item item);
    boolean deleteItem(String sku);
}