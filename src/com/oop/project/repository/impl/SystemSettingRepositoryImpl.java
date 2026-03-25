package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.SystemSetting;
import com.oop.project.repository.SystemSettingRepository;
import com.oop.project.util.DatabaseConnection;

public class SystemSettingRepositoryImpl implements SystemSettingRepository {

    @Override
    public List<SystemSetting> findAll() {
        List<SystemSetting> settings = new ArrayList<>();
        String sql = """
                SELECT setting_key, setting_value, description, created_date
                FROM system_settings
                ORDER BY setting_key
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) settings.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error in SystemSettingRepository.findAll(): " + e.getMessage());
        }
        return settings;
    }

    @Override
    public Optional<SystemSetting> findByKey(String key) {
        String sql = """
                SELECT setting_key, setting_value, description, created_date
                FROM system_settings
                WHERE setting_key = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in SystemSettingRepository.findByKey(): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean save(SystemSetting setting) {
        String sql = """
                INSERT INTO system_settings (setting_key, setting_value, description, created_date)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, setting.getSettingKey());
            ps.setString(2, setting.getSettingValue());
            ps.setString(3, setting.getDescription());
            ps.setTimestamp(4, setting.getCreatedDate());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in SystemSettingRepository.save(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(SystemSetting setting) {
        String sql = """
                UPDATE system_settings
                SET setting_value = ?, description = ?, created_date = ?
                WHERE setting_key = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, setting.getSettingValue());
            ps.setString(2, setting.getDescription());
            ps.setTimestamp(3, setting.getCreatedDate());
            ps.setString(4, setting.getSettingKey());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in SystemSettingRepository.update(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteByKey(String key) {
        String sql = "DELETE FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in SystemSettingRepository.deleteByKey(): " + e.getMessage());
            return false;
        }
    }

    private SystemSetting mapRow(ResultSet rs) throws SQLException {
        return new SystemSetting(
                rs.getString("setting_key"),
                rs.getString("setting_value"),
                rs.getString("description"),
                rs.getTimestamp("created_date")
        );
    }
}