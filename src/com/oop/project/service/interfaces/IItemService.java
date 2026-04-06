package com.oop.project.service.interfaces;

import com.oop.project.model.Item;
import com.oop.project.model.User;
import java.util.List;

public interface IItemService {
    List<Item> getAllItems();
    boolean addItem(Item item, User currentUser);
    boolean updateItem(Item item, User currentUser);
    boolean deleteItem(String sku);
    boolean addStock(String sku, int amount, User currentUser);
}