package com.oop.project.service.interfaces;

import com.oop.project.model.Item;
import com.oop.project.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * FR-2: Item Catalog Management
 *
 * Handles all business logic for the product/item catalog,
 * including adding, updating, removing items, and price management.
 *
 * Validation rules applied (see ValidationRules.java):
 * - SKU must match SKU_PATTERN (uppercase letters, digits, hyphens; 3–20 chars)
 * - SKU must be unique (no duplicates)
 * - Price must be between MIN_PRICE (0.01) and MAX_PRICE (999999.99)
 *
 * @author Lan - Service Layer
 */
public interface ItemService {

    /**
     * Retrieve all items from the catalog.
     *
     * @return list of all Item objects
     */
    List<Item> getAllItems();

    /**
     * Find a specific item by its SKU code.
     *
     * FR-2.2: Items are identified by SKU code.
     *
     * @param sku  the item's SKU code (e.g., "SHIRT-001")
     * @return Optional<Item>, empty if not found
     */
    Optional<Item> getItemBySku(String sku);

    /**
     * Add a new item to the catalog.
     *
     * FR-2.1: The system shall allow adding items to the catalog.
     * FR-2.2: Stores item name, unit price, category, and SKU code.
     * FR-2.3: The system shall prevent duplicate SKU codes.
     *
     * @param item  the Item to add (must have unique itemSku)
     * @return true if added successfully
     * @throws IllegalArgumentException if SKU is invalid, duplicate, or price is out of range
     */
    boolean addItem(Item item);

    /**
     * Update an existing item's information (name, category, stock).
     *
     * FR-2.1: The system shall allow updating items.
     * Note: To update price specifically, use updatePrice() which enforces Admin role.
     *
     * @param item  the Item with updated fields (itemSku must match an existing record)
     * @return true if updated successfully
     * @throws IllegalArgumentException if item not found or data is invalid
     */
    boolean updateItem(Item item);

    /**
     * Remove an item from the catalog by SKU.
     *
     * FR-2.1: The system shall allow removing items from the catalog.
     *
     * @param sku    the SKU code of the item to remove
     * @param actor  the currently logged-in User performing the action
     * @return true if removed successfully
     * @throws SecurityException if actor does not have ADMIN role
     * @throws IllegalArgumentException if item not found
     */
    boolean removeItem(String sku, User actor);

    /**
     * Update the price of an item. Restricted to ADMIN role only.
     *
     * FR-2.4: The system shall allow Admin users to modify the price of any item.
     * FR-0.4: Admin users shall be allowed to modify product prices.
     *
     * @param sku       the SKU code of the item to update
     * @param newPrice  the new unit price (must be > 0 and <= MAX_PRICE)
     * @param actor     the currently logged-in User (must be ADMIN)
     * @return true if price updated successfully
     * @throws SecurityException if actor is not ADMIN
     * @throws IllegalArgumentException if SKU not found or price is invalid
     */
    boolean updatePrice(String sku, BigDecimal newPrice, User actor);

    /**
     * Check if a given SKU code is already in use.
     *
     * FR-2.3: The system shall prevent duplicate SKU codes.
     *
     * @param sku  the SKU code to check
     * @return true if the SKU already exists in the database
     */
    boolean isSkuDuplicate(String sku);

    /**
     * Search items by name or category (partial match).
     *
     * @param keyword  the search keyword
     * @return list of matching Item objects
     */
    List<Item> searchItems(String keyword);
}
