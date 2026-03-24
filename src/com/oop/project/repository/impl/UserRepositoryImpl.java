package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import com.oop.project.repository.UserRepository;
import com.oop.project.util.DatabaseConnection;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = """
                SELECT user_id, user_name, user_password, user_role, created_date, last_login
                FROM users
                ORDER BY user_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) users.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error in UserRepository.findAll(): " + e.getMessage());
        }
        return users;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = """
                SELECT user_id, user_name, user_password, user_role, created_date, last_login
                FROM users
                WHERE user_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in UserRepository.findById(): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean save(User user) {
        String sql = """
                INSERT INTO users (user_name, user_password, user_role, created_date, last_login)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getUserPassword());
            ps.setString(3, user.getUserRole().name());
            ps.setTimestamp(4, user.getCreatedDate());
            ps.setTimestamp(5, user.getLastLogin());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in UserRepository.save(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(User user) {
        String sql = """
                UPDATE users
                SET user_name = ?, user_password = ?, user_role = ?, created_date = ?, last_login = ?
                WHERE user_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getUserPassword());
            ps.setString(3, user.getUserRole().name());
            ps.setTimestamp(4, user.getCreatedDate());
            ps.setTimestamp(5, user.getLastLogin());
            ps.setInt(6, user.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in UserRepository.update(): " + e.getMessage());
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
            System.err.println("Error in UserRepository.deleteById(): " + e.getMessage());
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("user_name"),
                rs.getString("user_password"),
                UserRole.valueOf(rs.getString("user_role").toUpperCase()),
                rs.getTimestamp("created_date"),
                rs.getTimestamp("last_login")
        );
    }
}