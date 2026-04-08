package com.oop.project.service.impl;

import com.oop.project.exception.DuplicateException;
import com.oop.project.exception.ResourceNotFoundException;
import com.oop.project.exception.ValidationException;
import com.oop.project.model.AuditLog;
import com.oop.project.model.Item;
import com.oop.project.model.User;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.ItemRepository;
import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.repository.impl.ItemRepositoryImpl;
import com.oop.project.service.interfaces.IItemService;
import com.oop.project.util.Validator;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class ItemServiceImpl implements IItemService {

    private final ItemRepository itemRepo;
    private final AuditLogRepository auditLogRepo;

    public ItemServiceImpl() {
        this.itemRepo = new ItemRepositoryImpl();
        this.auditLogRepo = new AuditLogRepositoryImpl();
    }

    public ItemServiceImpl(ItemRepository itemRepo, AuditLogRepository auditLogRepo) {
        this.itemRepo = itemRepo;
        this.auditLogRepo = auditLogRepo;
    }

    @Override
    public List<Item> getAllItems() {
        return itemRepo.findAll();
    }

    @Override
    public boolean addItem(Item item, User currentUser) {
        if (item == null) throw new ValidationException("Item cannot be null.");
        if (currentUser == null) throw new ValidationException("Current user must not be null.");
        // Validate SKU
        if (!Validator.isValidSku(item.getItemSku())) {
            throw new ValidationException("Invalid SKU format. Must be uppercase letters, digits, and hyphens (3-20 chars).");
        }
        // Check duplicate SKU
        if (itemRepo.isSkuExists(item.getItemSku())) {
            throw new DuplicateException("SKU already exists: " + item.getItemSku());
        }
        // Validate name, category, price, stock
        if (Validator.checkEmpty(item.getItemName())) {
            throw new ValidationException("Item name cannot be empty.");
        }
        if (Validator.checkEmpty(item.getCategory())) {
            throw new ValidationException("Category cannot be empty.");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.valueOf(Validator.MIN_PRICE)) < 0 ||
                item.getUnitPrice().compareTo(BigDecimal.valueOf(Validator.MAX_PRICE)) > 0) {
            throw new ValidationException("Price must be between " + Validator.MIN_PRICE + " and " + Validator.MAX_PRICE);
        }
        if (item.getStockQuantity() < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }
        boolean result = itemRepo.insert(item);
        if (result) {
            logAudit(currentUser, "ADD_ITEM", "ITEM", item.getItemSku());
        }
        return result;
    }

    @Override
    public boolean updateItem(Item item, User currentUser) {
        if (item == null) throw new ValidationException("Item cannot be null.");
        if (currentUser == null) throw new ValidationException("Current user must not be null.");
        Item existing = itemRepo.findBySku(item.getItemSku());
        if (existing == null) {
            throw new ResourceNotFoundException("Item not found with SKU: " + item.getItemSku());
        }
        // Validate fields (except SKU, which is unchanged)
        if (Validator.checkEmpty(item.getItemName())) {
            throw new ValidationException("Item name cannot be empty.");
        }
        if (Validator.checkEmpty(item.getCategory())) {
            throw new ValidationException("Category cannot be empty.");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.valueOf(Validator.MIN_PRICE)) < 0 ||
                item.getUnitPrice().compareTo(BigDecimal.valueOf(Validator.MAX_PRICE)) > 0) {
            throw new ValidationException("Price must be between " + Validator.MIN_PRICE + " and " + Validator.MAX_PRICE);
        }
        if (item.getStockQuantity() < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }
        boolean result = itemRepo.update(item);
        if (result) {
            logAudit(currentUser, "UPDATE_ITEM", "ITEM", item.getItemSku());
        }
        return result;
    }

    @Override
    public boolean deleteItem(String sku) {
        if (Validator.checkEmpty(sku)) throw new ValidationException("SKU cannot be empty.");
        Item existing = itemRepo.findBySku(sku);
        if (existing == null) {
            throw new ResourceNotFoundException("Item not found with SKU: " + sku);
        }
        // Note: no user parameter here, but we may still want to log? Interface doesn't have user.
        // We'll just delete without audit log (or you can add audit if needed, but interface doesn't require user)
        return itemRepo.delete(sku);
    }

    @Override
    public boolean addStock(String sku, int amount, User currentUser) {
        if (Validator.checkEmpty(sku)) throw new ValidationException("SKU cannot be empty.");
        if (amount <= 0) throw new ValidationException("Amount to add must be positive.");
        if (currentUser == null) throw new ValidationException("Current user must not be null.");
        Item item = itemRepo.findBySku(sku);
        if (item == null) {
            throw new ResourceNotFoundException("Item not found with SKU: " + sku);
        }
        boolean result = itemRepo.updateStock(sku, amount);
        if (result) {
            logAudit(currentUser, "ADD_STOCK", "ITEM", sku + " +" + amount);
        }
        return result;
    }

    private void logAudit(User user, String action, String targetType, String targetId) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setActions(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        auditLogRepo.insert(log);
    }
}