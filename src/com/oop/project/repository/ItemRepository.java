package com.oop.project.repository;

import java.util.List;
import java.util.Optional;

import com.oop.project.model.Item;

public interface ItemRepository {
    List<Item> findAll();
    Optional<Item> findBySku(String sku);
    boolean save(Item item);
    boolean update(Item item);
    boolean deleteBySku(String sku);
}