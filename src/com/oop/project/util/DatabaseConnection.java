package com.oop.project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DatabaseConnection {
    // 1. Các thông số kết nối
    private static String URL = "jdbc:postgresql://localhost:5432/ecommerce_db";
    private static String USER = "postgres";
    private static String PASSWORD = "password";

    private static Connection connection = null;

    static {
        loadEnv();
    }

    private static void loadEnv() {
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Bỏ qua dòng trống hoặc comment
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    
                    if (key.equals("DB_URL")) URL = value;
                    else if (key.equals("DB_USER")) USER = value;
                    else if (key.equals("DB_PASSWORD")) PASSWORD = value;
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Không tìm thấy file .env, sử dụng cấu hình mặc định.");
        }
    }

    // 2. PHƯƠNG THỨC LẤY KẾT NỐI (Phải có chữ static)
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Lỗi kết nối: " + e.getMessage());
        }
        return connection;
    }

    // 3. PHƯƠNG THỨC ĐÓNG KẾT NỐI (Phải có chữ static)
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Đã đóng kết nối Database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4. HÀM MAIN ĐỂ CHẠY THỬ (Checkpoint Tuần 2)
    public static void main(String[] args) {
        System.out.println("Đang kiểm tra kết nối...");
        Connection conn = getConnection(); // Gọi hàm này không còn bị lỗi "undefined" nữa

        if (conn != null) {
            System.out.println("✅ CHÚC MỪNG: Kết nối thành công tới PostgreSQL!");
            closeConnection();
        } else {
            System.out.println("❌ THẤT BẠI: Kiểm tra lại Driver hoặc cấu hình Database.");
        }
    }
}