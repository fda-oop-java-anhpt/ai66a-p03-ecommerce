package com.oop.project.service.impl;

import com.oop.project.exception.DuplicateException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.AuditLog;
import com.oop.project.model.Item;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.ItemRepository;
import com.oop.project.service.interfaces.IItemService;
import com.oop.project.util.Validator;

import java.util.List;

public class ItemServiceImpl implements IItemService {

    private final ItemRepository      itemRepo;
    private final AuditLogRepository  auditRepo;

    public ItemServiceImpl(ItemRepository itemRepo, AuditLogRepository auditRepo) {
        this.itemRepo  = itemRepo;
        this.auditRepo = auditRepo;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void log(User user, String action, String targetId) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUser(user);
            entry.setActions(action);
            entry.setTargetType("ITEM");
            entry.setTargetId(targetId);
            auditRepo.insert(entry);
        } catch (Exception e) {
            // Audit failure must NOT break the main operation
            e.printStackTrace();
        }
    }

    // ── IItemService ──────────────────────────────────────────────────────────
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
        boolean ok = itemRepo.insert(item);
        if (ok) log(currentUser, "ADD_ITEM: " + item.getItemName(), item.getItemSku());
        return ok;
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
        // Price change: admin only
        if (existing.getUnitPrice().compareTo(item.getUnitPrice()) != 0) {
            if (currentUser.getUserRole() != UserRole.ADMIN) {
                throw new SecurityException("Only Admin users can modify item prices.");
            }
        }
        // Detect what changed for a descriptive log message
        StringBuilder changes = new StringBuilder("UPDATE_ITEM");
        if (!existing.getItemName().equals(item.getItemName()))
            changes.append(" | Name: ").append(existing.getItemName()).append("→").append(item.getItemName());
        if (existing.getStockQuantity() != item.getStockQuantity())
            changes.append(" | Stock: ").append(existing.getStockQuantity()).append("→").append(item.getStockQuantity());
        if (existing.getUnitPrice().compareTo(item.getUnitPrice()) != 0)
            changes.append(" | Price: ").append(existing.getUnitPrice()).append("→").append(item.getUnitPrice());
        if (existing.getCategory() != null && !existing.getCategory().equals(item.getCategory()))
            changes.append(" | Category: ").append(existing.getCategory()).append("→").append(item.getCategory());

        boolean ok = itemRepo.update(item);
        if (ok) log(currentUser, changes.toString(), item.getItemSku());
        return ok;
    }

    @Override
    public boolean deleteItem(String sku) {
        // deleteItem doesn't receive a User in the current interface signature;
        // if a user context is needed later, extend the interface.
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
        boolean ok = itemRepo.updateStock(sku, amount);
        if (ok) log(currentUser,
                "ADD_STOCK: +" + amount + " (was " + existing.getStockQuantity()
                        + " → now " + (existing.getStockQuantity() + amount) + ")",
                sku);
        return ok;
    }
}