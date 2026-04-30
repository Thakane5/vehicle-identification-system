package com.example.vehicleidentification.DataAccessObject;

import com.example.vehicleidentification.database.DBConnection;
import com.example.vehicleidentification.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {


    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM customer ORDER BY customer_id");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Get customers error: " + e.getMessage());
        }
        return list;
    }

    public Customer getCustomerByUserId(String userId) {
        if (userId == null || userId.isBlank()) return null;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM customer WHERE user_id = ?");
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Get customer by userId error: " + e.getMessage());
        }
        return null;
    }

    public Customer getCustomerByCustomerId(int customerId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM customer WHERE customer_id = ?");
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Get customer by id error: " + e.getMessage());
        }
        return null;
    }

    public int getTotalCustomers() {
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM customer");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Count customers error: " + e.getMessage());
        }
        return 0;
    }

    public List<Customer> searchCustomers(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = """
                SELECT * FROM customer
                WHERE LOWER(name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)
                ORDER BY customer_id
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            String w = "%" + keyword + "%";
            stmt.setString(1, w);
            stmt.setString(2, w);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Search customers error: " + e.getMessage());
        }
        return list;
    }


    public boolean addCustomerNoLogin(Customer c) {
        String sql = """
                INSERT INTO customer (user_id, name, address, phone, email)
                VALUES (NULL, ?, ?, ?, ?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, c.getName());
            stmt.setString(2, c.getAddress());
            stmt.setString(3, c.getPhone());
            stmt.setString(4, c.getEmail());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add customer (no login) error: " + e.getMessage());
            return false;
        }
    }


    public boolean addCustomer(Customer c) {
        String sql = """
                INSERT INTO customer (user_id, name, address, phone, email)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            // setString handles NULL automatically when userId is null
            if (c.getUserId() == null || c.getUserId().isBlank())
                stmt.setNull(1, Types.VARCHAR);
            else
                stmt.setString(1, c.getUserId());
            stmt.setString(2, c.getName());
            stmt.setString(3, c.getAddress());
            stmt.setString(4, c.getPhone());
            stmt.setString(5, c.getEmail());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add customer error: " + e.getMessage());
            return false;
        }
    }


    public boolean updateCustomer(Customer c) {
        String sql = """
                UPDATE customer SET name=?, address=?, phone=?, email=?
                WHERE customer_id=?
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, c.getName());
            stmt.setString(2, c.getAddress());
            stmt.setString(3, c.getPhone());
            stmt.setString(4, c.getEmail());
            stmt.setInt   (5, c.getCustomerId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Update customer error: " + e.getMessage());
            return false;
        }
    }


    public boolean grantLoginAccess(int customerId, String userId) {
        String sql = "UPDATE customer SET user_id = ? WHERE customer_id = ?";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setInt   (2, customerId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Grant login error: " + e.getMessage());
            return false;
        }
    }


    public boolean deleteCustomer(int customerId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM customer WHERE customer_id=?");
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Delete customer error: " + e.getMessage());
            return false;
        }
    }


    private Customer mapRow(ResultSet rs) throws SQLException {
        String userId = null;
        try { userId = rs.getString("user_id"); } catch (SQLException ignored) {}
        return new Customer(
                rs.getInt("customer_id"),
                userId,   // may be null — that's fine
                rs.getString("name"),
                rs.getString("address") != null ? rs.getString("address") : "",
                rs.getString("phone")   != null ? rs.getString("phone")   : "",
                rs.getString("email")   != null ? rs.getString("email")   : ""
        );
    }
}