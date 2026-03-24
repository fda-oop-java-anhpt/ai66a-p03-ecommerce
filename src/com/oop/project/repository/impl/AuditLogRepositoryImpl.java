package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.AuditLog;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.AuditLogRepository;
import com.oop.project.util.DatabaseConnection;

public class AuditLogRepositoryImpl implements AuditLogRepository {

    @Override
    public List<AuditLog> findAll() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = """
                SELECT a.log_id, a.user_id, a.actions, a.target_type, a.target_id, a.created_date,
                       u.user_name, u.user_password, u.user_role, u.created_date AS user_created_date, u.last_login
                FROM audit_logs a
                JOIN users u ON a.user_id = u.user_id
                ORDER BY a.log_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) logs.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error in AuditLogRepository.findAll(): " + e.getMessage());
        }
        return logs;
    }

    @Override
    public Optional<AuditLog> findById(int logId) {
        String sql = """
                SELECT a.log_id, a.user_id, a.actions, a.target_type, a.target_id, a.created_date,
                       u.user_name, u.user_password, u.user_role, u.created_date AS user_created_date, u.last_login
                FROM audit_logs a
                JOIN users u ON a.user_id = u.user_id
                WHERE a.log_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, logId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in AuditLogRepository.findById(): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<AuditLog> findByUserId(int userId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = """
                SELECT a.log_id, a.user_id, a.actions, a.target_type, a.target_id, a.created_date,
                       u.user_name, u.user_password, u.user_role, u.created_date AS user_created_date, u.last_login
                FROM audit_logs a
                JOIN users u ON a.user_id = u.user_id
                WHERE a.user_id = ?
                ORDER BY a.log_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) logs.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in AuditLogRepository.findByUserId(): " + e.getMessage());
        }
        return logs;
    }

    @Override
    public boolean save(AuditLog log) {
        String sql = """
                INSERT INTO audit_logs (user_id, actions, target_type, target_id, created_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, log.getUser().getUserId());
            ps.setString(2, log.getActions());
            ps.setString(3, log.getTargetType());
            ps.setString(4, log.getTargetId());
            ps.setTimestamp(5, log.getCreatedDate());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in AuditLogRepository.save(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(int logId) {
        String sql = "DELETE FROM audit_logs WHERE log_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, logId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in AuditLogRepository.deleteById(): " + e.getMessage());
            return false;
        }
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("user_id"),
                rs.getString("user_name"),
                rs.getString("user_password"),
                UserRole.valueOf(rs.getString("user_role").toUpperCase()),
                rs.getTimestamp("user_created_date"),
                rs.getTimestamp("last_login")
        );

        return new AuditLog(
                rs.getInt("log_id"),
                user,
                rs.getString("actions"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getTimestamp("created_date")
        );
    }
}