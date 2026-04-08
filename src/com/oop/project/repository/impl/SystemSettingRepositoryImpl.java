package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oop.project.model.SystemSetting;
import com.oop.project.repository.interfaces.SystemSettingRepository;
import com.oop.project.util.DatabaseConnection;


public class SystemSettingRepositoryImpl implements SystemSettingRepository {

    // ==================== Find setting by key ====================
    @Override
    public SystemSetting findByKey(String key) {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new SystemSetting(
                        rs.getString("setting_key"),
                        rs.getString("setting_value"),
                        rs.getString("description"),
                        rs.getTimestamp("created_date")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== Update setting value ====================
    
    @Override
    public boolean update(String key, String value) {
        String sql = "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, key);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}