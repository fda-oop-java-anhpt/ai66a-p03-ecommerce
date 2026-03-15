package com.oop.project.repository.impl;

import com.oop.project.model.Order;
import com.oop.project.repository.OrderRepository;
import com.oop.project.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepository {

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT order_id, customer_id, coupon_code, tax_rate, discount_amount,
                       discount_info, status, order_date, subtotal, final_total
                FROM orders
                ORDER BY order_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    @Override
    public Optional<Order> findById(int id) {
        String sql = """
                SELECT order_id, customer_id, coupon_code, tax_rate, discount_amount,
                       discount_info, status, order_date, subtotal, final_total
                FROM orders
                WHERE order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error in findById(): " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean save(Order order) {
        String sql = """
                INSERT INTO orders (customer_id, coupon_code, tax_rate, discount_amount,
                                    discount_info, status, order_date, subtotal, final_total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (order.getCustomerId() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, order.getCustomerId());
            }

            ps.setString(2, order.getCouponCode());
            ps.setBigDecimal(3, order.getTaxRate());
            ps.setBigDecimal(4, order.getDiscountAmount());
            ps.setString(5, order.getDiscountInfo());
            ps.setString(6, order.getStatus());
            ps.setTimestamp(7, order.getOrderDate());
            ps.setBigDecimal(8, order.getSubtotal());
            ps.setBigDecimal(9, order.getFinalTotal());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in save(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Order order) {
        String sql = """
                UPDATE orders
                SET customer_id = ?, coupon_code = ?, tax_rate = ?, discount_amount = ?,
                    discount_info = ?, status = ?, order_date = ?, subtotal = ?, final_total = ?
                WHERE order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (order.getCustomerId() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, order.getCustomerId());
            }

            ps.setString(2, order.getCouponCode());
            ps.setBigDecimal(3, order.getTaxRate());
            ps.setBigDecimal(4, order.getDiscountAmount());
            ps.setString(5, order.getDiscountInfo());
            ps.setString(6, order.getStatus());
            ps.setTimestamp(7, order.getOrderDate());
            ps.setBigDecimal(8, order.getSubtotal());
            ps.setBigDecimal(9, order.getFinalTotal());
            ps.setInt(10, order.getOrderId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in update(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in deleteById(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("order_id");

        int rawCustomerId = rs.getInt("customer_id");
        Integer customerId = rs.wasNull() ? null : rawCustomerId;

        String couponCode = rs.getString("coupon_code");
        var taxRate = rs.getBigDecimal("tax_rate");
        var discountAmount = rs.getBigDecimal("discount_amount");
        String discountInfo = rs.getString("discount_info");
        String status = rs.getString("status");
        Timestamp orderDate = rs.getTimestamp("order_date");
        var subtotal = rs.getBigDecimal("subtotal");
        var finalTotal = rs.getBigDecimal("final_total");

        return new Order(
                orderId,
                customerId,
                couponCode,
                taxRate,
                discountAmount,
                discountInfo,
                status,
                orderDate,
                subtotal,
                finalTotal
        );
    }
}