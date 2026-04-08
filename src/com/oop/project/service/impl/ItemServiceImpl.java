package com.oop.project.service.impl;

import com.oop.project.model.AuditLog;
import com.oop.project.model.Item;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.impl.AuditLogRepositoryImpl;
import com.oop.project.repository.impl.ItemRepositoryImpl;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.repository.interfaces.ItemRepository;
import com.oop.project.service.interfaces.ItemService;
import com.oop.project.util.Validator;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ItemService.
 *
 * FR-2: Item Catalog Management
 *
 * Responsibilities:
 * - CRUD operations on items/products (FR-2.1)
 * - Store and validate item name, unitPrice, category, SKU (FR-2.2)
 * - Prevent duplicate SKU codes (FR-2.3)
 * - Allow Admin-only price updates (FR-2.4)
 * - Audit log all admin actions
 *
 * @author Lan - Service Layer
 */
public class ItemServiceImpl implements ItemService {

    // ── Dependencies ──────────────────────────────────────────────
    private final ItemRepository     itemRepository;
    private final AuditLogRepository auditLogRepository;

    // ── Constructor ───────────────────────────────────────────────
    public ItemServiceImpl() {
        this.itemRepository     = new ItemRepositoryImpl();
        this.auditLogRepository = new AuditLogRepositoryImpl();
    }

    public ItemServiceImpl(ItemRepository itemRepository,
                           AuditLogRepository auditLogRepository) {
        this.itemRepository     = itemRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public Optional<Item> getItemBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) return Optional.empty();
        return itemRepository.findBySku(sku.trim().toUpperCase());
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE — FR-2.1 + FR-2.2 + FR-2.3
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean addItem(Item item) {
        validateItem(item);

        // FR-2.3: Prevent duplicate SKU
        if (isSkuDuplicate(item.getItemSku())) {
            throw new IllegalArgumentException(
                "SKU already exists: " + item.getItemSku() +
                ". Please use a unique SKU code.");
        }

        return itemRepository.save(item);
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE — FR-2.1
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean updateItem(Item item) {
        itemRepository.findBySku(item.getItemSku())
            .orElseThrow(() -> new IllegalArgumentException(
                "Item not found with SKU: " + item.getItemSku()));

        validateItem(item);
        return itemRepository.update(item);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE — FR-2.1
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean removeItem(String sku, User actor) {
        requireAdmin(actor, "DELETE_ITEM");

        itemRepository.findBySku(sku)
            .orElseThrow(() -> new IllegalArgumentException(
                "Item not found with SKU: " + sku));

        return itemRepository.deleteBySku(sku);
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE PRICE — FR-2.4 (ADMIN ONLY)
    // ─────────────────────────────────────────────────────────────

    /**
     * Update the unit price of an item. Restricted to ADMIN role only.
     * Records the action in AuditLog.
     */
    @Override
    public boolean updatePrice(String sku, BigDecimal newPrice, User actor) {
        // FR-2.4 + FR-0.4: Enforce ADMIN role
        requireAdmin(actor, "UPDATE_PRICE");

        // Validate price range
        if (newPrice == null ||
            newPrice.compareTo(BigDecimal.valueOf(Validator.MIN_PRICE)) < 0 ||
            newPrice.compareTo(BigDecimal.valueOf(Validator.MAX_PRICE)) > 0) {
            throw new IllegalArgumentException(
                "Price must be between " + Validator.MIN_PRICE +
                " and " + Validator.MAX_PRICE);
        }

        Item item = itemRepository.findBySku(sku)
            .orElseThrow(() -> new IllegalArgumentException(
                "Item not found with SKU: " + sku));

        item.setUnitPrice(newPrice);
        boolean result = itemRepository.update(item);

        if (result) {
            // Audit log the price change
            AuditLog log = new AuditLog(
                0, actor,
                "UPDATE_PRICE: " + sku + " → " + newPrice,
                "ITEM", sku,
                Timestamp.from(Instant.now())
            );
            auditLogRepository.save(log);
        }

        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // SKU DUPLICATE CHECK — FR-2.3
    // ─────────────────────────────────────────────────────────────

    @Override
    public boolean isSkuDuplicate(String sku) {
        if (sku == null || sku.trim().isEmpty()) return false;
        return itemRepository.findBySku(sku.trim().toUpperCase()).isPresent();
    }

    // ─────────────────────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Item> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllItems();

        String lower = keyword.trim().toLowerCase();
        return itemRepository.findAll().stream()
            .filter(i ->
                i.getItemName().toLowerCase().contains(lower) ||
                i.getCategory().toLowerCase().contains(lower) ||
                i.getItemSku().toLowerCase().contains(lower)
            )
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Validate item fields against Validator.
     * Throws IllegalArgumentException with clear message if invalid.
     */
    private void validateItem(Item item) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null.");

        // Validate SKU format (FR-2.2)
        String sku = item.getItemSku();
        if (sku == null || !Validator.SKU_PATTERN.matcher(sku.trim()).matches()) {
            throw new IllegalArgumentException(
                "Invalid SKU format. Must be uppercase letters, digits, and hyphens (3-20 chars). E.g., SHIRT-001");
        }

        // Validate item name
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }

        // Validate category
        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Item category cannot be empty.");
        }

        // Validate price (FR-2.2)
        if (item.getUnitPrice() == null ||
            item.getUnitPrice().compareTo(BigDecimal.valueOf(Validator.MIN_PRICE)) < 0 ||
            item.getUnitPrice().compareTo(BigDecimal.valueOf(Validator.MAX_PRICE)) > 0) {
            throw new IllegalArgumentException(
                "Unit price must be between " + Validator.MIN_PRICE +
                " and " + Validator.MAX_PRICE);
        }
    }

    /**
     * Enforce ADMIN role. Throws SecurityException if actor is not Admin.
     */
    private void requireAdmin(User actor, String action) {
        if (actor == null || actor.getUserRole() != UserRole.ADMIN) {
            throw new SecurityException(
                "Action '" + action + "' requires ADMIN role. " +
                "Current user: " + (actor != null ? actor.getUserRole() : "null"));
        }
    }
}
