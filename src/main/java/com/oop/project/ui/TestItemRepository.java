package com.oop.project.ui;

import com.oop.project.repository.ItemRepository;
import com.oop.project.repository.impl.ItemRepositoryImpl;

public class TestItemRepository {

    public static void main(String[] args) {

        ItemRepository repo = new ItemRepositoryImpl();

        System.out.println("Testing ItemRepository...");
        repo.findAll().forEach(System.out::println);

    }
}