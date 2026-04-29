package com.example.vehicleidentification.DataAccessObject;

import com.example.vehicleidentification.database.DBConnection;
import com.example.vehicleidentification.model.ServiceRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRecordDAO {

    public boolean addServiceRecord(ServiceRecord r) {
        String sql = """
                INSERT INTO servicerecord (vehicle_id, service_date, service_type,
                    description, cost)
                VALUES (?,?,?,?,?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt   (1, r.getVehicleId());
            stmt.setDate  (2, Date.valueOf(r.getServiceDate()));
            stmt.setString(3, r.getServiceType());
            stmt.setString(4, r.getDescription());
            stmt.setDouble(5, r.getCost());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Add service record: " + e.getMessage());
            return false;
        }
    }

    public List<ServiceRecord> getAllServiceRecords() {
        List<ServiceRecord> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM servicerecord ORDER BY service_date DESC");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get service records: " + e.getMessage());
        }
        return list;
    }

    public List<ServiceRecord> getServiceRecordsByVehicle(int vehicleId) {
        List<ServiceRecord> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM servicerecord WHERE vehicle_id=? ORDER BY service_date DESC");
            stmt.setInt(1, vehicleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get records by vehicle: " + e.getMessage());
        }
        return list;
    }

    public boolean updateServiceRecord(ServiceRecord r) {
        String sql = """
                UPDATE servicerecord SET service_date=?, service_type=?,
                    description=?, cost=? WHERE service_id=?
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate  (1, Date.valueOf(r.getServiceDate()));
            stmt.setString(2, r.getServiceType());
            stmt.setString(3, r.getDescription());
            stmt.setDouble(4, r.getCost());
            stmt.setInt   (5, r.getServiceId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Update service record: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteServiceRecord(int serviceId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM servicerecord WHERE service_id=?");
            stmt.setInt(1, serviceId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Delete service record: " + e.getMessage());
            return false;
        }
    }

    private ServiceRecord mapRow(ResultSet rs) throws SQLException {
        return new ServiceRecord(
                rs.getInt("service_id"),
                rs.getInt("vehicle_id"),
                rs.getDate("service_date").toLocalDate(),
                rs.getString("service_type"),
                rs.getString("description") != null ? rs.getString("description") : "",
                rs.getDouble("cost")
        );
    }
}