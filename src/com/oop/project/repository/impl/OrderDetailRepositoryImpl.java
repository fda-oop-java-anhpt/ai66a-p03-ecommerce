package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.Item;
import com.oop.project.model.OrderDetail;
import com.oop.project.repository.OrderDetailRepository;
import com.oop.project.util.DatabaseConnection;

public class OrderDetailRepositoryImpl implements OrderDetailRepository {

    @Override
    public List<OrderDetail> findAll() {
        List<OrderDetail> details = new ArrayList<>();
        String sql = """
                SELECT od.order_detail_id, od.order_id, od.item_sku, od.quantity, od.price_at_time,
                       i.item_name, i.category, i.unit_price, i.stock_quantity
                FROM order_details od
                JOIN items i ON od.item_sku = i.item_sku
                ORDER BY od.order_detail_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) details.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error in OrderDetailRepository.findAll(): " + e.getMessage());
        }
        return details;
    }

    @Override
    public List<OrderDetail> findByOrderId(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = """
                SELECT od.order_detail_id, od.order_id, od.item_sku, od.quantity, od.price_at_time,
                       i.item_name, i.category, i.unit_price, i.stock_quantity
                FROM order_details od
                JOIN items i ON od.item_sku = i.item_sku
                WHERE od.order_id = ?
                ORDER BY od.order_detail_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) details.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in OrderDetailRepository.findByOrderId(): " + e.getMessage());
        }
        return details;
    }

    @Override
    public Optional<OrderDetail> findById(int orderDetailId) {
        String sql = """
                SELECT od.order_detail_id, od.order_id, od.item_sku, od.quantity, od.price_at_time,
                       i.item_name, i.category, i.unit_price, i.stock_quantity
                FROM order_details od
                JOIN items i ON od.item_sku = i.item_sku
                WHERE od.order_detail_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderDetailId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in OrderDetailRepository.findById(): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean save(OrderDetail detail) {
        String sql = """
                INSERT INTO order_details (order_id, item_sku, quantity, price_at_time)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, detail.getOrderId());
            ps.setString(2, detail.getItem().getItemSku());
            ps.setInt(3, detail.getQuantity());
            ps.setBigDecimal(4, detail.getPriceAtTime());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in OrderDetailRepository.save(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(OrderDetail detail) {
        String sql = """
                UPDATE order_details
                SET order_id = ?, item_sku = ?, quantity = ?, price_at_time = ?
                WHERE order_detail_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, detail.getOrderId());
            ps.setString(2, detail.getItem().getItemSku());
            ps.setInt(3, detail.getQuantity());
            ps.setBigDecimal(4, detail.getPriceAtTime());
            ps.setInt(5, detail.getOrderDetailId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in OrderDetailRepository.update(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(int orderDetailId) {
        String sql = "DELETE FROM order_details WHERE order_detail_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderDetailId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in OrderDetailRepository.deleteById(): " + e.getMessage());
            return false;
        }
    }

    private OrderDetail mapRow(ResultSet rs) throws SQLException {
        Item item = new Item(
                rs.getString("item_sku"),
                rs.getString("item_name"),
                rs.getString("category"),
                rs.getBigDecimal("unit_price"),
                rs.getInt("stock_quantity")
        );

        return new OrderDetail(
                rs.getInt("order_detail_id"),
                rs.getInt("order_id"),
                item,
                rs.getInt("quantity"),
                rs.getBigDecimal("price_at_time")
        );
    }
}