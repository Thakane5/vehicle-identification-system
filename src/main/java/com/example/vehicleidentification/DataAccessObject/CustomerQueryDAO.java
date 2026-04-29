package com.example.vehicleidentification.DataAccessObject;

import com.example.vehicleidentification.database.DBConnection;
import com.example.vehicleidentification.model.CustomerQuery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerQueryDAO {

    public boolean addQuery(CustomerQuery q) {
        String sql = """
                INSERT INTO customerquery (customer_id, user_id, vehicle_id,
                    query_date, query_text, response_text, status)
                VALUES (?,?,?,?,?,?,?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt   (1, q.getCustomerId());
            stmt.setInt   (2, q.getUserId());          // int — numeric DB key
            stmt.setInt   (3, q.getVehicleId());
            stmt.setDate  (4, Date.valueOf(q.getQueryDate()));
            stmt.setString(5, q.getQueryText());
            stmt.setString(6, q.getResponseText() != null ? q.getResponseText() : "");
            stmt.setString(7, q.getStatus()       != null ? q.getStatus()       : "Pending");
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add query error: " + e.getMessage());
            return false;
        }
    }

    public List<CustomerQuery> getAllQueries() {
        List<CustomerQuery> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM customerquery ORDER BY query_date DESC");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Get queries error: " + e.getMessage());
        }
        return list;
    }

    public List<CustomerQuery> getQueriesByCustomer(int customerId) {
        List<CustomerQuery> list = new ArrayList<>();
        String sql = """
                SELECT * FROM customerquery WHERE customer_id=?
                ORDER BY query_date DESC
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Get queries by customer error: " + e.getMessage());
        }
        return list;
    }

    public boolean respondToQuery(int queryId, String response) {
        String sql = """
                UPDATE customerquery SET response_text=?, status='Resolved'
                WHERE query_id=?
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, response);
            stmt.setInt   (2, queryId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Respond to query error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteQuery(int queryId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM customerquery WHERE query_id=?");
            stmt.setInt(1, queryId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Delete query error: " + e.getMessage());
            return false;
        }
    }

    private CustomerQuery mapRow(ResultSet rs) throws SQLException {
        CustomerQuery q = new CustomerQuery(
                rs.getInt("query_id"),
                rs.getInt("customer_id"),
                rs.getInt("vehicle_id"),
                rs.getDate("query_date").toLocalDate(),
                rs.getString("query_text"),
                rs.getString("response_text") != null ? rs.getString("response_text") : "",
                rs.getString("status")        != null ? rs.getString("status")        : "Pending"
        );
        try { q.setUserId(rs.getInt("user_id")); } catch (SQLException ignored) {}
        return q;
    }
}