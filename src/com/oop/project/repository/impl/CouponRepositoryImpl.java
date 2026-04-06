package com.oop.project.repository.impl;

import com.oop.project.model.Coupon;
import com.oop.project.model.DiscountType;
import com.oop.project.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CouponRepositoryImpl {

    // ==================== HELPER: Map ResultSet → Coupon ====================
    private Coupon mapRow(ResultSet rs) throws SQLException {
        return new Coupon(
                rs.getString("coupon_code"),
                rs.getBigDecimal("discount_value"),
                DiscountType.valueOf(rs.getString("discount_type")),
                rs.getBigDecimal("min_order_value"),
                rs.getTimestamp("created_date"),
                rs.getDate("expiry_date"),
                rs.getBoolean("is_active")
        );
    }

    // ==================== List all coupons ====================
    public List<Coupon> findAll() {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT * FROM coupons ORDER BY coupon_code";
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

    // ==================== Find by coupon code ====================
    public Coupon findByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE coupon_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== FR-4.2: Find active, non-expired coupons ====================
    public List<Coupon> findActiveCoupons() {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT * FROM coupons WHERE is_active = TRUE AND expiry_date >= CURRENT_DATE ORDER BY coupon_code";
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

    // ==================== FR-4.1: Insert coupon ====================
    public boolean insert(Coupon c) {
        String sql = "INSERT INTO coupons (coupon_code, discount_value, discount_type, min_order_value, expiry_date, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCouponCode());
            ps.setBigDecimal(2, c.getDiscountValue());
            ps.setString(3, c.getDiscountType().name());
            ps.setBigDecimal(4, c.getMinOrderValue());
            ps.setDate(5, c.getExpiryDate());
            ps.setBoolean(6, c.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== Update coupon ====================
    public boolean update(Coupon c) {
        String sql = "UPDATE coupons SET discount_value = ?, discount_type = ?, min_order_value = ?, expiry_date = ?, is_active = ? WHERE coupon_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, c.getDiscountValue());
            ps.setString(2, c.getDiscountType().name());
            ps.setBigDecimal(3, c.getMinOrderValue());
            ps.setDate(4, c.getExpiryDate());
            ps.setBoolean(5, c.isActive());
            ps.setString(6, c.getCouponCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}