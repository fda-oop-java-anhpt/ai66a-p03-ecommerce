package com.oop.project.ui;

import java.math.BigDecimal;

import com.oop.project.model.Item;
import com.oop.project.model.OrderDetail;
import com.oop.project.repository.ItemRepository;
import com.oop.project.repository.OrderDetailRepository;
import com.oop.project.repository.impl.ItemRepositoryImpl;
import com.oop.project.repository.impl.OrderDetailRepositoryImpl;

public class TestOrderDetailRepository {
    public static void main(String[] args) {

        OrderDetailRepository detailRepo = new OrderDetailRepositoryImpl();
        ItemRepository itemRepo = new ItemRepositoryImpl();

        System.out.println("=== ORDER DETAIL: FIND ALL ===");
        detailRepo.findAll().forEach(System.out::println);

        System.out.println("\n=== ORDER DETAIL: FIND BY ORDER ID (1) ===");
        detailRepo.findByOrderId(1).forEach(System.out::println);

        System.out.println("\n=== ORDER DETAIL: SAVE ===");
        Item item = itemRepo.findBySku("IP15PM-256").orElse(null);
        if (item == null) {
            System.out.println("Item SKU IP15PM-256 not found. Change SKU to one that exists.");
            return;
        }

        OrderDetail od = new OrderDetail(
                0,
                1,
                item,
                2,
                new BigDecimal("29500000.00")
        );

        System.out.println("Saved: " + detailRepo.save(od));

        System.out.println("\n=== ORDER DETAIL: FIND BY ORDER ID (1) AFTER SAVE ===");
        detailRepo.findByOrderId(1).forEach(System.out::println);
    }
}