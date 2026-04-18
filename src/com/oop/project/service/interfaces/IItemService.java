package com.oop.project.service.interfaces;

import com.oop.project.model.Item;
import com.oop.project.model.User;
import java.util.List;

public interface IItemService {
    List<Item> getAllItems();
    List<Item> getAllActiveItems();
    boolean addItem(Item item, User currentUser);
    boolean updateItem(Item item, User currentUser);
    boolean deleteItem(String sku, User actor);
    boolean setItemStatus(String sku, boolean isActive, User actor);
    boolean addStock(String sku, int amount, User currentUser);
}