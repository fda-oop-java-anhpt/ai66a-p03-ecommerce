package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.Coupon;
import com.oop.project.model.Customer;
import com.oop.project.model.DiscountType;
import com.oop.project.model.Order;
import com.oop.project.model.OrderStatus;
import com.oop.project.repository.OrderRepository;
import com.oop.project.util.DatabaseConnection;

public class OrderRepositoryImpl implements OrderRepository {

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT o.order_id, o.customer_id, o.coupon_code, o.tax_rate, o.discount_amount,
                       o.discount_info, o.status, o.subtotal, o.final_total, o.order_date,
                       c.customer_name, c.phone, c.email, c.address, c.created_date AS customer_created_date,
                       cp.discount_value, cp.discount_type, cp.min_order_value,
                       cp.created_date AS coupon_created_date, cp.expiry_date, cp.is_active
                FROM orders o
                LEFT JOIN customers c ON o.customer_id = c.customer_id
                LEFT JOIN coupons cp ON o.coupon_code = cp.coupon_code
                ORDER BY o.order_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in OrderRepository.findAll(): " + e.getMessage());
        }

        return orders;
    }

    @Override
    public Optional<Order> findById(int orderId) {
        String sql = """
                SELECT o.order_id, o.customer_id, o.coupon_code, o.tax_rate, o.discount_amount,
                       o.discount_info, o.status, o.subtotal, o.final_total, o.order_date,
                       c.customer_name, c.phone, c.email, c.address, c.created_date AS customer_created_date,
                       cp.discount_value, cp.discount_type, cp.min_order_value,
                       cp.created_date AS coupon_created_date, cp.expiry_date, cp.is_active
                FROM orders o
                LEFT JOIN customers c ON o.customer_id = c.customer_id
                LEFT JOIN coupons cp ON o.coupon_code = cp.coupon_code
                WHERE o.order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in OrderRepository.findById(): " + e.getMessage());
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

            ps.setInt(1, order.getCustomer().getCustomerId());

            if (order.getCoupon() == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, order.getCoupon().getCouponCode());
            }

            ps.setBigDecimal(3, order.getTaxRate());
            ps.setBigDecimal(4, order.getDiscountAmount());
            ps.setString(5, order.getDiscountInfo());
            ps.setString(6, order.getStatus().name());
            ps.setTimestamp(7, order.getOrderDate());
            ps.setBigDecimal(8, order.getSubtotal());
            ps.setBigDecimal(9, order.getFinalTotal());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in OrderRepository.save(): " + e.getMessage());
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

            ps.setInt(1, order.getCustomer().getCustomerId());

            if (order.getCoupon() == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, order.getCoupon().getCouponCode());
            }

            ps.setBigDecimal(3, order.getTaxRate());
            ps.setBigDecimal(4, order.getDiscountAmount());
            ps.setString(5, order.getDiscountInfo());
            ps.setString(6, order.getStatus().name());
            ps.setTimestamp(7, order.getOrderDate());
            ps.setBigDecimal(8, order.getSubtotal());
            ps.setBigDecimal(9, order.getFinalTotal());
            ps.setInt(10, order.getOrderId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in OrderRepository.update(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(int orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in OrderRepository.deleteById(): " + e.getMessage());
            return false;
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        // Customer
        Customer customer = new Customer(
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getTimestamp("customer_created_date")
        );

        // Coupon (nullable)
        Coupon coupon = null;
        String couponCode = rs.getString("coupon_code");
        if (couponCode != null) {
            coupon = new Coupon(
                    couponCode,
                    rs.getBigDecimal("discount_value"),
                    DiscountType.valueOf(rs.getString("discount_type")),
                    rs.getBigDecimal("min_order_value"),
                    rs.getTimestamp("coupon_created_date"),
                    rs.getDate("expiry_date"),
                    rs.getBoolean("is_active")
            );
        }

        return new Order(
                rs.getInt("order_id"),
                customer,
                coupon,
                rs.getBigDecimal("tax_rate"),
                rs.getBigDecimal("discount_amount"),
                rs.getString("discount_info"),
                OrderStatus.valueOf(rs.getString("status").toUpperCase()),
                rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("final_total"),
                rs.getTimestamp("order_date")
        );
    }
}