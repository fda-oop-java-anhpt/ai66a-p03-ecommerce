package com.oop.project.repository.impl;

import com.oop.project.model.*;
import com.oop.project.repository.OrderRepository;
import com.oop.project.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepositoryImpl implements OrderRepository {
    private final OrderDetailRepositoryImpl orderDetailRepo = new OrderDetailRepositoryImpl();

    // ==================== HELPER: Map ResultSet → Order (with Customer & Coupon) ====================
    private Order mapRow(ResultSet rs) throws SQLException {
        // Build Customer object
        Customer customer = null;
        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            customer = new Customer(
                    customerId,
                    rs.getString("customer_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getTimestamp("customer_created")
            );
        }

        // Build Coupon object (nullable)
        Coupon coupon = null;
        String couponCode = rs.getString("coupon_code");
        if (couponCode != null) {
            coupon = new Coupon();
            coupon.setCouponCode(couponCode);
        }

        return new Order(
                rs.getInt("order_id"),
                customer,
                coupon,
                rs.getBigDecimal("tax_rate"),
                rs.getBigDecimal("discount_amount"),
                rs.getString("discount_info"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("final_total"),
                rs.getTimestamp("order_date")
        );
    }

    // Base SELECT with JOIN to customers
    private static final String BASE_SELECT =
            "SELECT o.*, " +
            "c.customer_name, c.phone, c.email, c.address, c.created_date AS customer_created " +
            "FROM orders o " +
            "LEFT JOIN customers c ON o.customer_id = c.customer_id ";

    // ==================== FR-5.1: List all orders ====================
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY o.order_date DESC";
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

    // ==================== Find by order ID (with details) ====================
    public Order findById(int id) {
        String sql = BASE_SELECT + "WHERE o.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = mapRow(rs);
                // Load order details with item info
                order.setOrderItems(orderDetailRepo.findByOrderId(id));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== FR-1.4: Find orders by customer ID ====================
    public List<Order> findByCustomerId(int customerId) {
        List<Order> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE o.customer_id = ? ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = mapRow(rs);
                order.setOrderItems(orderDetailRepo.findByOrderId(order.getOrderId()));
                list.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================== FR-5.3: Search by customer name or order ID ====================
    public List<Order> searchByCustomerNameOrId(String keyword) {
        List<Order> list = new ArrayList<>();
        String sql = BASE_SELECT +
                "WHERE LOWER(c.customer_name) LIKE LOWER(?) OR CAST(o.order_id AS TEXT) LIKE ? " +
                "ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================== FR-5.2: Filter by status and/or date range ====================
    public List<Order> filterByStatusOrDateRange(String status, Timestamp from, Timestamp to) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            sql.append("AND o.status = ? ");
            params.add(status);
        }
        if (from != null) {
            sql.append("AND o.order_date >= ? ");
            params.add(from);
        }
        if (to != null) {
            sql.append("AND o.order_date <= ? ");
            params.add(to);
        }
        sql.append("ORDER BY o.order_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) {
                    ps.setString(i + 1, (String) p);
                } else if (p instanceof Timestamp) {
                    ps.setTimestamp(i + 1, (Timestamp) p);
                }
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================== FR-3.1: Insert order (returns generated order_id) ====================
    public int insert(Order order) {
        String sql = "INSERT INTO orders (customer_id, coupon_code, tax_rate, discount_amount, discount_info, status, subtotal, final_total) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomer().getCustomerId());

            if (order.getCoupon() != null) {
                ps.setString(2, order.getCoupon().getCouponCode());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }

            ps.setBigDecimal(3, order.getTaxRate());
            ps.setBigDecimal(4, order.getDiscountAmount());
            ps.setString(5, order.getDiscountInfo());
            ps.setString(6, order.getStatus().name());
            ps.setBigDecimal(7, order.getSubtotal());
            ps.setBigDecimal(8, order.getFinalTotal());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int orderId = keys.getInt(1);
                    // Insert order details
                    if (!order.getOrderItems().isEmpty()) {
                        orderDetailRepo.insertBatch(orderId, order.getOrderItems());
                    }
                    return orderId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ==================== FR-3.1: Update order header ====================
    public boolean update(Order order) {
        String sql = "UPDATE orders SET customer_id = ?, coupon_code = ?, tax_rate = ?, " +
                     "discount_amount = ?, discount_info = ?, status = ?, subtotal = ?, final_total = ? " +
                     "WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order.getCustomer().getCustomerId());

            if (order.getCoupon() != null) {
                ps.setString(2, order.getCoupon().getCouponCode());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }

            ps.setBigDecimal(3, order.getTaxRate());
            ps.setBigDecimal(4, order.getDiscountAmount());
            ps.setString(5, order.getDiscountInfo());
            ps.setString(6, order.getStatus().name());
            ps.setBigDecimal(7, order.getSubtotal());
            ps.setBigDecimal(8, order.getFinalTotal());
            ps.setInt(9, order.getOrderId());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                // Re-insert order details: clear old, insert new
                orderDetailRepo.deleteByOrderId(order.getOrderId());
                if (!order.getOrderItems().isEmpty()) {
                    orderDetailRepo.insertBatch(order.getOrderId(), order.getOrderItems());
                }
            }
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== FR-4.3: Update order status only ====================
    public boolean updateStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== FR-3.1: Delete order ====================
    public boolean delete(int orderId) {
        // order_details are ON DELETE CASCADE, so only delete the order
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== FR-5.4: Summary statistics ====================
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public java.math.BigDecimal sumRevenue() {
        String sql = "SELECT COALESCE(SUM(final_total), 0) FROM orders WHERE status = 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return java.math.BigDecimal.ZERO;
    }
}
