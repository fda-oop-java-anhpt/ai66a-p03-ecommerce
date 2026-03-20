package com.oop.project.service.interfaces;

import com.oop.project.model.Item;
import com.oop.project.model.User;
import com.oop.project.service.exception.ServiceException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for item/product management.
 * Handles CRUD operations with admin role verification for price updates.
 * 
 * @author Service Team
 * @version 1.0
 */
public interface ItemService {
    
    /**
     * Creates a new item with SKU duplicate check.
     * 
     * @param item the Item object to create
     * @return the created Item with validated data
     * @throws ServiceException if SKU already exists or validation fails
     */
    Item createItem(Item item) throws ServiceException;
    
    /**
     * Updates item information.
     * Price updates require admin role verification.
     * 
     * @param item the Item object with updated information
     * @param currentUser the user performing the update
     * @return the updated Item object
     * @throws ServiceException if validation fails or user lacks permission
     */
    Item updateItem(Item item, User currentUser) throws ServiceException;
    
    /**
     * Updates only the price of an item (ADMIN ONLY).
     * This method enforces strict admin role checking.
     * 
     * @param sku the SKU of the item
     * @param newPrice the new price to set
     * @param adminUser the admin user performing the update
     * @return true if update successful
     * @throws ServiceException if user is not admin or item not found
     */
    boolean updatePrice(String sku, BigDecimal newPrice, User adminUser) throws ServiceException;
    
    /**
     * Deletes an item by SKU.
     * Checks if item is used in any orders before deletion.
     * 
     * @param sku the SKU of item to delete
     * @return true if deletion successful
     * @throws ServiceException if item is in use or not found
     */
    boolean deleteItem(String sku) throws ServiceException;
    
    /**
     * Retrieves an item by SKU.
     * 
     * @param sku the item SKU
     * @return Item object if found, null otherwise
     */
    Item getItemBySku(String sku);
    
    /**
     * Retrieves all items in inventory.
     * 
     * @return List of all items
     */
    List<Item> getAllItems();
    
    /**
     * Searches items by keyword (name or category).
     * 
     * @param keyword the search keyword
     * @return List of matching items
     */
    List<Item> searchItems(String keyword);
    
    /**
     * Checks if an SKU already exists in the system.
     * Used for duplicate validation during item creation.
     * 
     * @param sku the SKU to check
     * @return true if SKU exists, false otherwise
     */
    boolean isSkuDuplicate(String sku);
    
    /**
     * Retrieves items by category.
     * 
     * @param category the category name
     * @return List of items in the category
     */
    List<Item> getItemsByCategory(String category);
    
    /**
     * Checks if item has sufficient stock.
     * 
     * @param sku the item SKU
     * @param quantity the quantity to check
     * @return true if stock is sufficient
     */
    boolean checkStockAvailability(String sku, int quantity);
    
    /**
     * Updates stock quantity after order placement or cancellation.
     * 
     * @param sku the item SKU
     * @param quantityChange the quantity to add (positive) or subtract (negative)
     * @return true if update successful
     * @throws ServiceException if insufficient stock or item not found
     */
    boolean updateStock(String sku, int quantityChange) throws ServiceException;
}
