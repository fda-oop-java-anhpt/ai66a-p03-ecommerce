package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.model.Item;
import com.oop.project.repository.interfaces.ItemRepository;
import com.oop.project.util.DatabaseConnection;



public class ItemRepositoryImpl implements ItemRepository {

    // ==================== HELPER: Map ResultSet → Item ====================
    private Item mapRow(ResultSet rs) throws SQLException {
        return new Item(
                rs.getString("item_sku"),
                rs.getString("item_name"),
                rs.getString("category"),
                rs.getBigDecimal("unit_price"),
                rs.getInt("stock_quantity")
        );
    }

    // ==================== List all items ====================
    @Override
    public List<Item> findAll() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY item_sku";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================== Find by SKU ====================
    @Override
    public Item findBySku(String sku) {
        String sql = "SELECT * FROM items WHERE item_sku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== FR-2.3: Check duplicate SKU ====================
    @Override
    public boolean isSkuExists(String sku) {
        String sql = "SELECT 1 FROM items WHERE item_sku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== FR-2.1: Insert item ====================
    @Override
    public boolean insert(Item item) {
        String sql = "INSERT INTO items (item_sku, item_name, category, unit_price, stock_quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemSku());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getCategory());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.setInt(5, item.getStockQuantity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== FR-2.1, FR-2.4: Update item ====================
    @Override
    public boolean update(Item item) {
        String sql = "UPDATE items SET item_name = ?, category = ?, unit_price = ?, stock_quantity = ? WHERE item_sku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getUnitPrice());
            ps.setInt(4, item.getStockQuantity());
            ps.setString(5, item.getItemSku());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== FR-2.1: Delete item ====================
    @Override
    public boolean delete(String sku) {
        String sql = "DELETE FROM items WHERE item_sku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== Update stock quantity ====================
    @Override
    public boolean updateStock(String sku, int quantityChange) {
        String sql = "UPDATE items SET stock_quantity = stock_quantity + ? WHERE item_sku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityChange);
            ps.setString(2, sku);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}