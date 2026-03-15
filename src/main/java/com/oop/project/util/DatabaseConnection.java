package com.oop.project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Thông tin kết nối PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/E-commerce";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456";

    private DatabaseConnection() {
        // tránh tạo object
    }

    /**
     * Lấy kết nối tới database
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Test kết nối database
     */
    public static void main(String[] args) {
        System.out.println("Checking PostgreSQL connection...");

        try (Connection conn = getConnection()) {

            if (conn != null) {
                System.out.println(" Connected successfully to PostgreSQL!");
                System.out.println("Database: " + conn.getCatalog());
            }

        } catch (SQLException e) {
            System.err.println(" Database connection failed!");
            e.printStackTrace();
        }
    }
}