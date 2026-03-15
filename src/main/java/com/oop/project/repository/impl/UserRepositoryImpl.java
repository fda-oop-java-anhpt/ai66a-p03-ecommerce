package com.oop.project.repository.impl;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.UserRepository;
import com.oop.project.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, user_name, user_role, created_date, last_login FROM users ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT user_id, user_name, user_role, created_date, last_login FROM users WHERE user_id = ?";

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
    public boolean save(User user) {
        String sql = "INSERT INTO users (user_name, user_role, created_date, last_login) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getRole().name());
            ps.setTimestamp(3, user.getCreatedDate());
            ps.setTimestamp(4, user.getLastLogin());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in save(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET user_name = ?, user_role = ?, created_date = ?, last_login = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getRole().name());
            ps.setTimestamp(3, user.getCreatedDate());
            ps.setTimestamp(4, user.getLastLogin());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in update(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";

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

    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String name = rs.getString("user_name");
        UserRole role = UserRole.valueOf(rs.getString("user_role").toUpperCase());
        Timestamp createdDate = rs.getTimestamp("created_date");
        Timestamp lastLogin = rs.getTimestamp("last_login");

        return new User(id, name, role, createdDate, lastLogin);
    }
}