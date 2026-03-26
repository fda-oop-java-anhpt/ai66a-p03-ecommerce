package com.oop.project.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.Item;
import com.oop.project.repository.ItemRepository;
import com.oop.project.service.ItemService;

public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public Optional<Item> getItemBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            System.out.println("SKU cannot be empty.");
            return Optional.empty();
        }
        return itemRepository.findBySku(sku);
    }

    @Override
    public boolean createItem(Item item) {
        if (!validateItem(item)) return false;
        if (itemRepository.findBySku(item.getItemSku()).isPresent()) {
            System.out.println("Item already exists.");
            return false;
        }
        return itemRepository.save(item);
    }

    @Override
    public boolean updateItem(Item item) {
        if (!validateItem(item)) return false;
        if (itemRepository.findBySku(item.getItemSku()).isEmpty()) {
            System.out.println("Item not found.");
            return false;
        }
        return itemRepository.update(item);
    }

    @Override
    public boolean deleteItem(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            System.out.println("SKU cannot be empty.");
            return false;
        }
        if (itemRepository.findBySku(sku).isEmpty()) {
            System.out.println("Item not found.");
            return false;
        }
        return itemRepository.deleteBySku(sku);
    }

    private boolean validateItem(Item item) {
        if (item == null) {
            System.out.println("Item cannot be null.");
            return false;
        }
        if (item.getItemSku() == null || item.getItemSku().trim().isEmpty()) {
            System.out.println("Item SKU cannot be empty.");
            return false;
        }
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            System.out.println("Item name cannot be empty.");
            return false;
        }
        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
            System.out.println("Category cannot be empty.");
            return false;
        }
        if (item.getUnitPrice() == null) {
            System.out.println("Unit price cannot be null.");
            return false;
        }
        if (item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Unit price must be greater than 0.");
            return false;
        }
        if (item.getStockQuantity() < 0) {
            System.out.println("Stock quantity cannot be negative.");
            return false;
        }
        return true;
    }
}