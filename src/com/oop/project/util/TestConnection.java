package com.oop.project.util;

import com.oop.project.model.User;
import com.oop.project.model.UserRole;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class TestConnection {
    public static void main(String[] args) {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT user_id, user_name, user_role, created_date, last_login FROM users";

        System.out.println("--- ĐANG TRUY VẤN DỮ LIỆU TỪ POSTGRESQL ---");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Ánh xạ (Map) dữ liệu từ dòng trong DB sang Object Java
                int id = rs.getInt("user_id");
                String name  = rs.getString("user_name");
                UserRole role = UserRole.valueOf(rs.getString("user_role"));
                Timestamp createdDate = rs.getTimestamp("created_date");
                Timestamp lastLogin = rs.getTimestamp("last_login");
                User user = new  User(id, name, role, createdDate, lastLogin);
                userList.add(user);
            }

            // In danh sách kết quả
            if (userList.isEmpty()) {
                System.out.println("⚠️ Database trống, hãy chạy file seed.sql trước!");
            } else {
                userList.forEach(System.out::println);
                System.out.println("\n✅ Thành công: Đã lôi được " + userList.size() + " người dùng lên Java!");
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}
