package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.model.Item;
import com.oop.project.model.OrderDetail;
import com.oop.project.repository.OrderDetailRepository;
import com.oop.project.util.DatabaseConnection;

public class OrderDetailRepositoryImpl implements OrderDetailRepository {

    // ==================== Find all details for an order (JOIN items) ====================
    public List<OrderDetail> findByOrderId(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT od.*, i.item_name, i.category, i.unit_price, i.stock_quantity " +
                     "FROM order_details od " +
                     "JOIN items i ON od.item_sku = i.item_sku " +
                     "WHERE od.order_id = ? " +
                     "ORDER BY od.order_detail_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item(
                        rs.getString("item_sku"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getBigDecimal("unit_price"),
                        rs.getInt("stock_quantity")
                );
                OrderDetail detail = new OrderDetail(
                        rs.getInt("order_detail_id"),
                        rs.getInt("order_id"),
                        item,
                        rs.getInt("quantity"),
                        rs.getBigDecimal("price_at_time")
                );
                list.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================== Bulk insert order details ====================
    public boolean insertBatch(int orderId, List<OrderDetail> details) {
        String sql = "INSERT INTO order_details (order_id, item_sku, quantity, price_at_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderDetail d : details) {
                ps.setInt(1, orderId);
                ps.setString(2, d.getItem().getItemSku());
                ps.setInt(3, d.getQuantity());
                ps.setBigDecimal(4, d.getPriceAtTime());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            // All rows must be inserted successfully
            for (int r : results) {
                if (r <= 0 && r != Statement.SUCCESS_NO_INFO) return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== Delete all details for an order ====================
    public boolean deleteByOrderId(int orderId) {
        String sql = "DELETE FROM order_details WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
