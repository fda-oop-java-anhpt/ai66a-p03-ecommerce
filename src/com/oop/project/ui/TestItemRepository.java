package com.oop.project.ui;

import java.math.BigDecimal;

import com.oop.project.model.Item;
import com.oop.project.repository.ItemRepository;
import com.oop.project.repository.impl.ItemRepositoryImpl;

public class TestItemRepository {
    public static void main(String[] args) {

        ItemRepository repo = new ItemRepositoryImpl();

        System.out.println("=== ITEM: FIND ALL ===");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n=== ITEM: FIND BY SKU (IP15PM-256) ===");
        System.out.println(repo.findBySku("IP15PM-256").orElse(null));

        System.out.println("\n=== ITEM: SAVE ===");
        String sku = "TEST-" + System.currentTimeMillis();
        Item item = new Item(
                sku,
                "Test Item",
                "Test",
                new BigDecimal("100000.00"),
                10
        );
        System.out.println("Saved: " + repo.save(item));

        System.out.println("\n=== ITEM: FIND BY SKU (new) ===");
        System.out.println(repo.findBySku(sku).orElse(null));
    }
}