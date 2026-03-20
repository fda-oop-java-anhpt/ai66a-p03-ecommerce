package com.oop.project.service.impl;

import com.oop.project.model.Item;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.service.interfaces.ItemService;
import com.oop.project.exception.ServiceException;
import com.oop.project.exception.ValidationException;
import com.oop.project.util.Validator;
import com.oop.project.util.ValidationRules;
// import com.oop.project.repository.ItemRepository; // Uncomment Week 4
import java.math.BigDecimal;
import java.util.*;

/**
 * Implementation of ItemService interface.
 * Handles item CRUD operations with admin role verification for price updates.
 * 
 * NOTE: Uses stub data for Week 1-3. Integrate with Repository in Week 4.
 * 
 * @author Service Team
 * @version 1.0
 */
public class ItemServiceImpl implements ItemService {
    
    // TODO Week 4: Inject repository via constructor
    // private final ItemRepository itemRepository;
    
    /**
     * Creates a new item with SKU duplicate check.
     * 
     * @param item the Item object to create
     * @return the created Item with validated data
     * @throws ServiceException if SKU already exists or validation fails
     */
    @Override
    public Item createItem(Item item) throws ServiceException {
        try {
            // Validate item data
            validateItemData(item);
            
            // Check for duplicate SKU
            if (isSkuDuplicate(item.getItemSku())) {
                throw new ValidationException("SKU", "SKU already exists: " + item.getItemSku());
            }
            
            // TODO Week 4: return itemRepository.save(item);
            
            // STUB
            return item;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to create item: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates item information.
     * Price updates require admin role verification.
     * 
     * @param item the Item object with updated information
     * @param currentUser the user performing the update
     * @return the updated Item object
     * @throws ServiceException if validation fails or user lacks permission
     */
    @Override
    public Item updateItem(Item item, User currentUser) throws ServiceException {
        try {
            // Validate item data
            validateItemData(item);
            
            // TODO Week 4: Get existing item to check if price changed
            // Item existingItem = itemRepository.findBySku(item.getItemSku());
            // if (existingItem != null && 
            //     existingItem.getUnitPrice().compareTo(item.getUnitPrice()) != 0) {
            //     // Price changed - verify admin role
            //     if (currentUser.getUserRole() != UserRole.ADMIN) {
            //         throw new ServiceException("Only admins can update item prices");
            //     }
            // }
            
            // Verify admin role if updating price
            if (currentUser == null || currentUser.getUserRole() != UserRole.ADMIN) {
                throw new ServiceException("Only admins can update items");
            }
            
            // TODO Week 4: return itemRepository.update(item);
            
            // STUB
            return item;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to update item: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates only the price of an item (ADMIN ONLY).
     * 
     * @param sku the SKU of the item
     * @param newPrice the new price to set
     * @param adminUser the admin user performing the update
     * @return true if update successful
     * @throws ServiceException if user is not admin or item not found
     */
    @Override
    public boolean updatePrice(String sku, BigDecimal newPrice, User adminUser) throws ServiceException {
        try {
            // Validate inputs
            Validator.validateSku(sku);
            Validator.validatePrice(newPrice, "New Price");
            
            // Verify admin role
            if (adminUser == null || adminUser.getUserRole() != UserRole.ADMIN) {
                throw new ServiceException("Only admins can update prices. Permission denied.");
            }
            
            // TODO Week 4: Item item = itemRepository.findBySku(sku);
            // if (item == null) {
            //     throw new ServiceException("Item not found with SKU: " + sku);
            // }
            // return itemRepository.updatePrice(sku, newPrice);
            
            // STUB
            System.out.println("Admin " + adminUser.getUserName() + " updated price for SKU " + sku);
            return true;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to update price: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes an item by SKU.
     * Checks if item is used in any orders before deletion.
     * 
     * @param sku the SKU of item to delete
     * @return true if deletion successful
     * @throws ServiceException if item is in use or not found
     */
    @Override
    public boolean deleteItem(String sku) throws ServiceException {
        try {
            Validator.validateSku(sku);
            
            // TODO Week 4: Check if item is used in orders
            // if (orderItemRepository.existsByItemSku(sku)) {
            //     throw new ServiceException("Cannot delete item that is used in orders");
            // }
            // return itemRepository.deleteBySku(sku);
            
            // STUB
            return true;
            
        } catch (Exception e) {
            throw new ServiceException("Failed to delete item: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves an item by SKU.
     * 
     * @param sku the item SKU
     * @return Item object if found, null otherwise
     */
    @Override
    public Item getItemBySku(String sku) {
        try {
            // TODO Week 4: return itemRepository.findBySku(sku);
            
            // STUB
            return null;
        } catch (Exception e) {
            System.err.println("Error retrieving item: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Retrieves all items in inventory.
     * 
     * @return List of all items
     */
    @Override
    public List<Item> getAllItems() {
        try {
            // TODO Week 4: return itemRepository.findAll();
            
            // STUB
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error retrieving items: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Searches items by keyword (name or category).
     * 
     * @param keyword the search keyword
     * @return List of matching items
     */
    @Override
    public List<Item> searchItems(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllItems();
            }
            
            // TODO Week 4: return itemRepository.searchByKeyword(keyword);
            
            // STUB
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error searching items: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Checks if an SKU already exists in the system.
     * 
     * @param sku the SKU to check
     * @return true if SKU exists, false otherwise
     */
    @Override
    public boolean isSkuDuplicate(String sku) {
        try {
            // TODO Week 4: return itemRepository.existsBySku(sku);
            
            // STUB
            return false;
        } catch (Exception e) {
            System.err.println("Error checking SKU: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves items by category.
     * 
     * @param category the category name
     * @return List of items in the category
     */
    @Override
    public List<Item> getItemsByCategory(String category) {
        try {
            // TODO Week 4: return itemRepository.findByCategory(category);
            
            // STUB
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error retrieving items by category: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Checks if item has sufficient stock.
     * 
     * @param sku the item SKU
     * @param quantity the quantity to check
     * @return true if stock is sufficient
     */
    @Override
    public boolean checkStockAvailability(String sku, int quantity) {
        try {
            // TODO Week 4: Item item = itemRepository.findBySku(sku);
            // return item != null && item.getStockQuantity() >= quantity;
            
            // STUB
            return true;
        } catch (Exception e) {
            System.err.println("Error checking stock: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates stock quantity after order placement or cancellation.
     * 
     * @param sku the item SKU
     * @param quantityChange the quantity to add (positive) or subtract (negative)
     * @return true if update successful
     * @throws ServiceException if insufficient stock or item not found
     */
    @Override
    public boolean updateStock(String sku, int quantityChange) throws ServiceException {
        try {
            Validator.validateSku(sku);
            
            // TODO Week 4: Item item = itemRepository.findBySku(sku);
            // if (item == null) {
            //     throw new ServiceException("Item not found: " + sku);
            // }
            // int newStock = item.getStockQuantity() + quantityChange;
            // if (newStock < 0) {
            //     throw new ServiceException("Insufficient stock for SKU: " + sku);
            // }
            // return itemRepository.updateStock(sku, newStock);
            
            // STUB
            return true;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to update stock: " + e.getMessage(), e);
        }
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    /**
     * Validates item data before create/update operations.
     * 
     * @param item the item to validate
     * @throws ValidationException if validation fails
     */
    private void validateItemData(Item item) throws ValidationException {
        if (item == null) {
            throw new ValidationException("Item object cannot be null");
        }
        
        // Validate SKU
        Validator.validateSku(item.getItemSku());
        
        // Validate name
        Validator.validateRequired(item.getItemName(), "Item Name");
        Validator.validateLength(item.getItemName(), "Item Name", 2, 100);
        
        // Validate price
        Validator.validatePrice(item.getUnitPrice(), "Unit Price");
        
        // Validate category
        Validator.validateRequired(item.getCategory(), "Category");
        
        // Validate stock quantity
        if (item.getStockQuantity() < 0) {
            throw new ValidationException("Stock Quantity", "Stock quantity cannot be negative");
        }
    }
}
