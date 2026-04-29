package com.example.vehicleidentification.DataAccessObject;

import com.example.vehicleidentification.database.DBConnection;
import com.example.vehicleidentification.model.PoliceReport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoliceReportDAO {

    public boolean addReport(PoliceReport r) {
        String sql = """
                INSERT INTO policereport (vehicle_id, user_id, report_date,
                    report_type, description, officer_name, station_name, case_number)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt   (1, r.getVehicleId());
            stmt.setString(2, r.getUserId());
            stmt.setDate  (3, Date.valueOf(r.getReportDate()));
            stmt.setString(4, r.getReportType());
            stmt.setString(5, r.getDescription());
            stmt.setString(6, r.getOfficerName());
            stmt.setString(7, r.getStationName());
            stmt.setString(8, r.getCaseNumber());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Add report: " + e.getMessage());
            return false;
        }
    }

    public List<PoliceReport> getAllReports() {
        List<PoliceReport> list = new ArrayList<>();
        String sql = """
                SELECT p.*, v.registration_number
                FROM policereport p
                LEFT JOIN vehicle v ON p.vehicle_id = v.vehicle_id
                ORDER BY p.report_date DESC
                """;
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get reports: " + e.getMessage());
        }
        return list;
    }

    public boolean updateReport(PoliceReport r) {
        String sql = """
                UPDATE policereport SET report_date=?, report_type=?,
                    description=?, officer_name=?, station_name=?, case_number=?
                WHERE report_id=?
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate  (1, Date.valueOf(r.getReportDate()));
            stmt.setString(2, r.getReportType());
            stmt.setString(3, r.getDescription());
            stmt.setString(4, r.getOfficerName());
            stmt.setString(5, r.getStationName());
            stmt.setString(6, r.getCaseNumber());
            stmt.setInt   (7, r.getReportId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Update report: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReport(int reportId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM policereport WHERE report_id=?");
            stmt.setInt(1, reportId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Delete report: " + e.getMessage());
            return false;
        }
    }

    public List<PoliceReport> searchReports(String kw) {
        List<PoliceReport> list = new ArrayList<>();
        String sql = """
                SELECT p.*, v.registration_number FROM policereport p
                LEFT JOIN vehicle v ON p.vehicle_id = v.vehicle_id
                WHERE v.registration_number ILIKE ? OR p.officer_name ILIKE ?
                   OR p.case_number ILIKE ? OR p.report_type ILIKE ?
                ORDER BY p.report_date DESC
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
            System.out.println("❌ Search reports: " + e.getMessage());
        }
        return list;
    }

    private PoliceReport mapRow(ResultSet rs) throws SQLException {
        String reg = "";
        try { reg = rs.getString("registration_number"); } catch (SQLException ignored) {}
        String userId = "";
        try { userId = rs.getString("user_id"); } catch (SQLException ignored) {}
        return new PoliceReport(
                rs.getInt("report_id"),
                rs.getInt("vehicle_id"),
                reg,
                userId,
                rs.getDate("report_date").toLocalDate(),
                rs.getString("report_type"),
                rs.getString("description")  != null ? rs.getString("description")  : "",
                rs.getString("officer_name") != null ? rs.getString("officer_name") : "",
                rs.getString("station_name") != null ? rs.getString("station_name") : "",
                rs.getString("case_number")  != null ? rs.getString("case_number")  : ""
        );
    }
}