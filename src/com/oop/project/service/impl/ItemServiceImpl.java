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

    private final ItemRepository itemRepo;
    private final AuditLogRepository auditRepo;

    public ItemServiceImpl(ItemRepository itemRepo, AuditLogRepository auditRepo) {
        this.itemRepo = itemRepo;
        this.auditRepo = auditRepo;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void log(User actor, String action, String targetId) {
        if (actor == null)
            return;
        try {
            AuditLog entry = new AuditLog();
            entry.setUser(actor);
            entry.setActions(action);
            entry.setTargetType("ITEM");
            entry.setTargetId(targetId);
            auditRepo.insert(entry);
        } catch (Exception e) {
            // Audit failure must NOT break the main operation
            System.err.println("Audit log failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── IItemService ──────────────────────────────────────────────────────────
    @Override
    public List<Item> getAllItems() {
        return itemRepo.findAll();
    }

    @Override
    public List<Item> getAllActiveItems() {
        return itemRepo.findAllActive();
    }

    @Override
    public boolean addItem(Item item, User currentUser) {
        if (currentUser == null || currentUser.getUserRole() != UserRole.ADMIN) {
            throw new SecurityException("Only Admin users can add new items.");
        }
        if (item == null || Validator.checkEmpty(item.getItemSku())) {
            throw new ValidationException("Item and SKU must not be null.");
        }
        if (itemRepo.isSkuExists(item.getItemSku())) {
            throw new DuplicateException("SKU '" + item.getItemSku() + "' already exists.");
        }
        boolean ok = itemRepo.insert(item);
        if (ok) {
            log(currentUser, "CREATE_ITEM", item.getItemSku());
        }
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
        boolean ok = itemRepo.update(item);
        if (ok) {
            log(currentUser, "UPDATE_ITEM", item.getItemSku());
        }
        return ok;
    }

    @Override
    public boolean deleteItem(String sku, User actor) {
        if (itemRepo.hasBeenOrdered(sku)) {
            throw new ValidationException("Cannot delete item: it has already been ordered.");
        }
        boolean ok = itemRepo.delete(sku);
        if (!ok) {
            throw new ValidationException("Failed to delete item from database.");
        }
        if (ok) {
            log(actor, "DELETE_ITEM", sku);
        }
        return ok;
    }

    @Override
    public boolean setItemStatus(String sku, boolean isActive, User actor) {
        boolean ok = itemRepo.updateStatus(sku, isActive);
        if (ok) {
            String act = isActive ? "ACTIVATE_ITEM" : "DEACTIVATE_ITEM";
            log(actor, act, sku);
        }
        return ok;
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
                        + " -> now " + (existing.getStockQuantity() + amount) + ")",
                sku);
        return ok;
    }
}