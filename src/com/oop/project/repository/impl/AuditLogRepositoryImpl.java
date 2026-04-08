package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.model.AuditLog;
import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.interfaces.AuditLogRepository;
import com.oop.project.util.DatabaseConnection;


public class AuditLogRepositoryImpl implements AuditLogRepository {

    // ==================== HELPER: Map ResultSet → AuditLog ====================
    private AuditLog mapRow(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("user_id"),
                rs.getString("user_name"),
                UserRole.valueOf(rs.getString("user_role")),
                rs.getTimestamp("user_created"),
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

    // Base SELECT with JOIN to users
    private static final String BASE_SELECT =
            "SELECT al.*, u.user_name, u.user_role, u.created_date AS user_created, u.last_login " +
            "FROM audit_logs al " +
            "JOIN users u ON al.user_id = u.user_id ";

    // ==================== FR-0.5, FR-4.4: Insert audit log ====================
    @Override
    public boolean insert(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, actions, target_type, target_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getUser().getUserId());
            ps.setString(2, log.getActions());
            ps.setString(3, log.getTargetType());
            ps.setString(4, log.getTargetId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== List all audit logs ====================
    @Override
    public List<AuditLog> findAll() {
        List<AuditLog> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY al.created_date DESC";
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
}