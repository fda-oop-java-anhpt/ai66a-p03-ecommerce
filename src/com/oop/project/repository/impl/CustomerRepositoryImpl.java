package com.oop.project.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oop.project.model.Customer;
import com.oop.project.repository.CustomerRepository;
import com.oop.project.util.DatabaseConnection;

public class CustomerRepositoryImpl implements CustomerRepository {

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = """
                SELECT customer_id, customer_name, phone, email, address, created_date
                FROM customers
                ORDER BY customer_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) customers.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("Error in CustomerRepository.findAll(): " + e.getMessage());
        }
        return customers;
    }

    @Override
    public Optional<Customer> findById(int id) {
        String sql = """
                SELECT customer_id, customer_name, phone, email, address, created_date
                FROM customers
                WHERE customer_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error in CustomerRepository.findById(): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean save(Customer customer) {
        String sql = """
                INSERT INTO customers (customer_name, phone, email, address, created_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setTimestamp(5, customer.getCreatedDate());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in CustomerRepository.save(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Customer customer) {
        String sql = """
                UPDATE customers
                SET customer_name = ?, phone = ?, email = ?, address = ?, created_date = ?
                WHERE customer_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setTimestamp(5, customer.getCreatedDate());
            ps.setInt(6, customer.getCustomerId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error in CustomerRepository.update(): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in CustomerRepository.deleteById(): " + e.getMessage());
            return false;
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getTimestamp("created_date")
        );
    }
}