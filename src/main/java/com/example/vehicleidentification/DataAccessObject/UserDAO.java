package com.example.vehicleidentification.DataAccessObject;

import com.example.vehicleidentification.database.DBConnection;
import com.example.vehicleidentification.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User loginByCustomId(String userId, String password, String role) {
        String sql = """
                SELECT * FROM users
                WHERE UPPER(user_id)  = UPPER(?)
                  AND password        = ?
                  AND UPPER(role)     = UPPER(?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId.trim());
            stmt.setString(2, password.trim());
            stmt.setString(3, role.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            System.out.println("❌ No match — userId: " + userId);
        } catch (SQLException e) {
            System.out.println("❌ Login error: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM users ORDER BY user_id");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get users: " + e.getMessage());
        }
        return list;
    }

    public List<User> getCustomerUsers() {
        List<User> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM users WHERE role='CUSTOMER' ORDER BY username");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get customer users: " + e.getMessage());
        }
        return list;
    }

     public boolean userIdExists(String userId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE UPPER(user_id) = UPPER(?)");
            stmt.setString(1, userId.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("❌ Check ID: " + e.getMessage());
        }
        return false;
    }

    public boolean createUser(User user) {
        if (userIdExists(user.getUserId())) {
            System.out.println("❌ ID already exists: " + user.getUserId());
            return false;
        }
        String sql = """
                INSERT INTO users (user_id, username, password, role, email)
                VALUES (?, ?, ?, ?, ?)
                """;
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUserId().trim().toUpperCase());
            stmt.setString(2, user.getUsername().trim());
            stmt.setString(3, user.getPassword().trim());
            stmt.setString(4, user.getRole().trim().toUpperCase());
            stmt.setString(5, user.getEmail() != null ? user.getEmail().trim() : "");
            stmt.executeUpdate();

            // If CUSTOMER — create customer record
            if ("CUSTOMER".equalsIgnoreCase(user.getRole())) {
                insertCustomer(conn, user);
            }

            conn.commit();
            System.out.println("✅ User created: " + user.getUserId());
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Create user: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); } }
        }
    }

    private void insertCustomer(Connection conn, User user) throws SQLException {
        String sql = """
                INSERT INTO customer (user_id, name, email, phone, address)
                VALUES (?, ?, ?, ?, ?)
                """;
        PreparedStatement s = conn.prepareStatement(sql);
        s.setString(1, user.getUserId().trim().toUpperCase());
        s.setString(2, user.getUsername().trim());
        s.setString(3, user.getEmail()  != null ? user.getEmail().trim()  : "");
        s.setString(4, user.getPhone()  != null ? user.getPhone().trim()  : "");
        s.setString(5, "");
        s.executeUpdate();
        System.out.println(" Customer record created for: " + user.getUserId());
    }

     public boolean updateUser(User user) {
        String sql = """
                UPDATE users SET username=?, password=?, role=?, email=?
                WHERE user_id=?
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername().trim());
            stmt.setString(2, user.getPassword().trim());
            stmt.setString(3, user.getRole().trim().toUpperCase());
            stmt.setString(4, user.getEmail() != null ? user.getEmail().trim() : "");
            stmt.setString(5, user.getUserId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Update user: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(String userId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM users WHERE user_id=?");
            stmt.setString(1, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Delete user: " + e.getMessage());
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String email = "";
        try { email = rs.getString("email"); } catch (SQLException ignored) {}
        return new User(
                rs.getString("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                email, ""
        );
    }

    /**
     * Inserts a record into the users table only — does NOT create a customer row.
     * Used when granting login access to an existing data-only customer.
     */
    public boolean insertUserOnly(String userId, String name, String password,
                                  String role, String email, String phone) {
        String sql = """
            INSERT INTO users (user_id, username, password, role, email, phone)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, name);
            stmt.setString(3, password); // hash this if your app hashes passwords
            stmt.setString(4, role);
            stmt.setString(5, email);
            stmt.setString(6, phone);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("insertUserOnly error: " + e.getMessage());
            return false;
        }
    }
}