package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.Coupon;
import com.oop.project.model.DiscountType;
import com.oop.project.repository.CouponRepository;
import com.oop.project.util.DatabaseConnection;

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

            while (rs.next()) coupons.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error in CouponRepository.findAll(): " + e.getMessage());
        }
        return coupons;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
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
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in CouponRepository.findByCode(): " + e.getMessage());
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
            // DB CHECK đang dùng 'Percent'/'Fixed' -> dùng name() của enum đúng y chang file: Percent, Fixed :contentReference[oaicite:1]{index=1}
            ps.setString(3, coupon.getDiscountType().name());
            ps.setBigDecimal(4, coupon.getMinOrderValue());
            ps.setTimestamp(5, coupon.getCreatedDate());
            ps.setDate(6, coupon.getExpiryDate());
            ps.setBoolean(7, coupon.isActive());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in CouponRepository.save(): " + e.getMessage());
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
            ps.setString(2, coupon.getDiscountType().name());
            ps.setBigDecimal(3, coupon.getMinOrderValue());
            ps.setTimestamp(4, coupon.getCreatedDate());
            ps.setDate(5, coupon.getExpiryDate());
            ps.setBoolean(6, coupon.isActive());
            ps.setString(7, coupon.getCouponCode());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in CouponRepository.update(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteByCode(String code) {
        String sql = "DELETE FROM coupons WHERE coupon_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in CouponRepository.deleteByCode(): " + e.getMessage());
            return false;
        }
    }

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
}