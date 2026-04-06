package com.oop.project.service.impl;

import com.oop.project.exception.DuplicateException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.Item;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.ItemRepository;
import com.oop.project.service.interfaces.IItemService;
import com.oop.project.util.Validator;

import java.util.List;

public class ItemServiceImpl implements IItemService {

    private final ItemRepository itemRepo;

    public ItemServiceImpl(ItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    @Override
    public List<Item> getAllItems() {
        return itemRepo.findAll();
    }

    @Override
    public boolean addItem(Item item, User currentUser) {
        if (item == null || Validator.checkEmpty(item.getItemSku())) {
            throw new ValidationException("Item and SKU must not be null.");
        }
        if (itemRepo.isSkuExists(item.getItemSku())) {
            throw new DuplicateException("SKU '" + item.getItemSku() + "' already exists.");
        }
        return itemRepo.insert(item);
    }

    @Override
    public boolean updateItem(Item item, User currentUser) {
        if (item == null || currentUser == null) {
            throw new ValidationException("Item and current user must not be null.");
        }
        Item existing = itemRepo.findBySku(item.getItemSku());
        if (existing == null) {
            throw new ValidationException("Item with SKU '" + item.getItemSku() + "' not found.");
        }
        if (existing.getUnitPrice().compareTo(item.getUnitPrice()) != 0) {
            if (currentUser.getUserRole() != UserRole.ADMIN) {
                throw new SecurityException("Only Admin users can modify item prices.");
            }
        }
        return itemRepo.update(item);
    }

    @Override
    public boolean deleteItem(String sku) {
        return itemRepo.delete(sku);
    }

    @Override
    public boolean addStock(String sku, int amount, User currentUser) {
        if (Validator.checkEmpty(sku) || currentUser == null) {
            throw new ValidationException("SKU and current user must not be null.");
        }
        if (amount <= 0) {
            throw new ValidationException("Amount must be greater than zero.");
        }
        Item existing = itemRepo.findBySku(sku);
        if (existing == null) {
            throw new ValidationException("Item with SKU '" + sku + "' not found.");
        }
        return itemRepo.updateStock(sku, amount);
    }
}