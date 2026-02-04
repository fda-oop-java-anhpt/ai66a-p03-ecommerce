package com.oop.project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // 1. Các thông số kết nối (Thay password của bạn vào đây)
    private static final String URL = "jdbc:postgresql://localhost:5432/ecommerce_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "081106"; 


    private static Connection connection = null;

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