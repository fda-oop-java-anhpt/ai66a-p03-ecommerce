package com.oop.project.repository.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.Item;
import com.oop.project.repository.ItemRepository;
import com.oop.project.util.DatabaseConnection;

public class ItemRepositoryImpl implements ItemRepository {

    @Override
    public List<Item> findAll() {

        List<Item> items = new ArrayList<>();

        String sql = """
                SELECT item_sku, item_name, category, unit_price, stock_quantity
                FROM items
                ORDER BY item_sku
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in findAll(): " + e.getMessage());
        }

        return items;
    }

    @Override
    public Optional<Item> findById(String id) {

        String sql = """
                SELECT item_sku, item_name, category, unit_price, stock_quantity
                FROM items
                WHERE item_sku = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error in findById(): " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public boolean save(Item item) {

        String sql = """
                INSERT INTO items (item_sku, item_name, category, unit_price, stock_quantity)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemSku());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getCategory());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.setInt(5, item.getStockQuantity());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in save(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Item item) {

        String sql = """
                UPDATE items
                SET item_name = ?, category = ?, unit_price = ?, stock_quantity = ?
                WHERE item_sku = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getUnitPrice());
            ps.setInt(4, item.getStockQuantity());
            ps.setString(5, item.getItemSku());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in update(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(String id) {

        String sql = "DELETE FROM items WHERE item_sku = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in deleteById(): " + e.getMessage());
            return false;
        }
    }

    private Item mapRow(ResultSet rs) throws SQLException {

        String sku = rs.getString("item_sku");
        String name = rs.getString("item_name");
        String category = rs.getString("category");
        BigDecimal price = rs.getBigDecimal("unit_price");
        int stock = rs.getInt("stock_quantity");

        return new Item(sku, name, category, price, stock);
    }
}