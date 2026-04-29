package com.example.vehicleidentification.DataAccessObject;

import com.example.vehicleidentification.database.DBConnection;
import com.example.vehicleidentification.model.Violation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ViolationDAO {

    public boolean addViolation(Violation v) {
        String sql = """
                INSERT INTO violation (vehicle_id, violation_date, violation_type,
                    description, fine_amount, status, officer_name, location)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt   (1, v.getVehicleId());
            stmt.setDate  (2, Date.valueOf(v.getViolationDate()));
            stmt.setString(3, v.getViolationType());
            stmt.setString(4, v.getDescription());
            stmt.setDouble(5, v.getFineAmount());
            stmt.setString(6, v.getStatus() != null ? v.getStatus() : "Unpaid");
            stmt.setString(7, v.getOfficerName());
            stmt.setString(8, v.getLocation());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Add violation: " + e.getMessage());
            return false;
        }
    }

    public List<Violation> getAllViolations() {
        List<Violation> list = new ArrayList<>();
        String sql = """
                SELECT v.*, vh.registration_number
                FROM violation v
                LEFT JOIN vehicle vh ON v.vehicle_id = vh.vehicle_id
                ORDER BY v.violation_date DESC
                """;
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get violations: " + e.getMessage());
        }
        return list;
    }

    public List<Violation> getViolationsByVehicle(int vehicleId) {
        List<Violation> list = new ArrayList<>();
        String sql = """
                SELECT v.*, vh.registration_number
                FROM violation v
                LEFT JOIN vehicle vh ON v.vehicle_id = vh.vehicle_id
                WHERE v.vehicle_id=?
                ORDER BY v.violation_date DESC
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vehicleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get violations by vehicle: " + e.getMessage());
        }
        return list;
    }

    public boolean updateViolation(Violation v) {
        String sql = """
                UPDATE violation SET violation_date=?, violation_type=?,
                    description=?, fine_amount=?, status=?,
                    officer_name=?, location=?
                WHERE violation_id=?
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate  (1, Date.valueOf(v.getViolationDate()));
            stmt.setString(2, v.getViolationType());
            stmt.setString(3, v.getDescription());
            stmt.setDouble(4, v.getFineAmount());
            stmt.setString(5, v.getStatus());
            stmt.setString(6, v.getOfficerName());
            stmt.setString(7, v.getLocation());
            stmt.setInt   (8, v.getViolationId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Update violation: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int violationId, String status) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE violation SET status=? WHERE violation_id=?");
            stmt.setString(1, status);
            stmt.setInt   (2, violationId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Update violation status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteViolation(int violationId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM violation WHERE violation_id=?");
            stmt.setInt(1, violationId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Delete violation: " + e.getMessage());
            return false;
        }
    }

    public int getUnpaidCount() {
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM violation WHERE status='Unpaid'");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("❌ Count unpaid: " + e.getMessage());
        }
        return 0;
    }

    public List<Violation> searchViolations(String kw) {
        List<Violation> list = new ArrayList<>();
        String sql = """
                SELECT v.*, vh.registration_number FROM violation v
                LEFT JOIN vehicle vh ON v.vehicle_id = vh.vehicle_id
                WHERE vh.registration_number ILIKE ? OR v.violation_type ILIKE ?
                   OR v.officer_name ILIKE ? OR v.location ILIKE ?
                ORDER BY v.violation_date DESC
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            String w = "%" + kw + "%";
            stmt.setString(1,w); stmt.setString(2,w);
            stmt.setString(3,w); stmt.setString(4,w);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Search violations: " + e.getMessage());
        }
        return list;
    }

    private Violation mapRow(ResultSet rs) throws SQLException {
        String reg = "";
        try { reg = rs.getString("registration_number"); } catch (SQLException ignored) {}
        return new Violation(
                rs.getInt("violation_id"),
                rs.getInt("vehicle_id"),
                reg,
                rs.getDate("violation_date").toLocalDate(),
                rs.getString("violation_type"),
                rs.getString("description") != null ? rs.getString("description") : "",
                rs.getDouble("fine_amount"),
                rs.getString("status"),
                rs.getString("officer_name") != null ? rs.getString("officer_name") : "",
                rs.getString("location")     != null ? rs.getString("location")     : ""
        );
    }
}