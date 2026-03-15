package com.oop.project.repository.impl;

import com.oop.project.model.Coupon;
import com.oop.project.repository.CouponRepository;
import com.oop.project.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CouponRepositoryImpl implements CouponRepository {

    @Override
    public List<Coupon> findAll() {
        List<Coupon> coupons = new ArrayList<>();

        String sql = """
                SELECT coupon_code, discount_value, discount_type, min_order_value,
                       created_date, expiry_date, is_active
                FROM coupons
                ORDER BY coupon_code
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                coupons.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }

        return coupons;
    }

    @Override
    public Optional<Coupon> findById(String code) {
        String sql = """
                SELECT coupon_code, discount_value, discount_type, min_order_value,
                       created_date, expiry_date, is_active
                FROM coupons
                WHERE coupon_code = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);

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
    public boolean save(Coupon coupon) {
        String sql = """
                INSERT INTO coupons (coupon_code, discount_value, discount_type,
                                     min_order_value, created_date, expiry_date, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, coupon.getCouponCode());
            ps.setBigDecimal(2, coupon.getDiscountValue());
            ps.setString(3, coupon.getDiscountType());
            ps.setBigDecimal(4, coupon.getMinOrderValue());
            ps.setTimestamp(5, coupon.getCreatedDate());
            ps.setDate(6, coupon.getExpiryDate());
            ps.setBoolean(7, coupon.isActive());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in save(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Coupon coupon) {
        String sql = """
                UPDATE coupons
                SET discount_value = ?, discount_type = ?, min_order_value = ?,
                    created_date = ?, expiry_date = ?, is_active = ?
                WHERE coupon_code = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, coupon.getDiscountValue());
            ps.setString(2, coupon.getDiscountType());
            ps.setBigDecimal(3, coupon.getMinOrderValue());
            ps.setTimestamp(4, coupon.getCreatedDate());
            ps.setDate(5, coupon.getExpiryDate());
            ps.setBoolean(6, coupon.isActive());
            ps.setString(7, coupon.getCouponCode());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in update(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(String code) {
        String sql = "DELETE FROM coupons WHERE coupon_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in deleteById(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Coupon mapRow(ResultSet rs) throws SQLException {
        String couponCode = rs.getString("coupon_code");
        var discountValue = rs.getBigDecimal("discount_value");
        String discountType = rs.getString("discount_type");
        var minOrderValue = rs.getBigDecimal("min_order_value");
        Timestamp createdDate = rs.getTimestamp("created_date");
        Date expiryDate = rs.getDate("expiry_date");
        boolean isActive = rs.getBoolean("is_active");

        return new Coupon(
                couponCode,
                discountValue,
                discountType,
                minOrderValue,
                createdDate,
                expiryDate,
                isActive
        );
    }
}