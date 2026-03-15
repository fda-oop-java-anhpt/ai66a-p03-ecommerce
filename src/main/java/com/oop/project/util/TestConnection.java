package com.oop.project.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;

public class TestConnection {

    public static void main(String[] args) {

        List<User> userList = new ArrayList<>();

        String sql = "SELECT user_id, user_name, user_role, created_date, last_login FROM users";

        System.out.println("Querying data from PostgreSQL...");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("user_id");
                String name = rs.getString("user_name");
                UserRole role = UserRole.valueOf(rs.getString("user_role").toUpperCase());
                Timestamp createdDate = rs.getTimestamp("created_date");
                Timestamp lastLogin = rs.getTimestamp("last_login");

                User user = new User(id, name, role, createdDate, lastLogin);
                userList.add(user);
            }

            if (userList.isEmpty()) {
                System.out.println("Database is empty. Please run the seed.sql file first.");
            } else {
                userList.forEach(System.out::println);
                System.out.println("\nSuccess: Retrieved " + userList.size() + " users from the database.");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Enum Error: user_role value in database does not match the UserRole enum.");
            e.printStackTrace();
        }
    }
}