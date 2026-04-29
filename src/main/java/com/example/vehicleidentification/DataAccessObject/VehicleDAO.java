package com.example.vehicleidentification.DataAccessObject;

import com.example.vehicleidentification.database.DBConnection;
import com.example.vehicleidentification.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public boolean addVehicle(Vehicle v) {
        String sql = """
                INSERT INTO vehicle (registration_number, make, model, year,
                    color, chassis_number, owner_id)
                VALUES (?,?,?,?,?,?,?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, v.getRegistrationNumber());
            stmt.setString(2, v.getMake());
            stmt.setString(3, v.getModel());
            stmt.setInt   (4, v.getYear());
            stmt.setString(5, v.getColor());
            stmt.setString(6, v.getChassisNumber());
            stmt.setInt   (7, v.getOwnerId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Add vehicle: " + e.getMessage());
            return false;
        }
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM vehicle ORDER BY vehicle_id");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get vehicles: " + e.getMessage());
        }
        return list;
    }

    public List<Vehicle> getVehiclesByCustomerId(int customerId) {
        List<Vehicle> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM vehicle WHERE owner_id=? ORDER BY vehicle_id");
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get vehicles by customer: " + e.getMessage());
        }
        return list;
    }

    public boolean updateVehicle(Vehicle v) {
        String sql = """
                UPDATE vehicle SET registration_number=?, make=?, model=?,
                    year=?, color=?, chassis_number=?, owner_id=?
                WHERE vehicle_id=?
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, v.getRegistrationNumber());
            stmt.setString(2, v.getMake());
            stmt.setString(3, v.getModel());
            stmt.setInt   (4, v.getYear());
            stmt.setString(5, v.getColor());
            stmt.setString(6, v.getChassisNumber());
            stmt.setInt   (7, v.getOwnerId());
            stmt.setInt   (8, v.getVehicleId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Update vehicle: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteVehicle(int vehicleId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM vehicle WHERE vehicle_id=?");
            stmt.setInt(1, vehicleId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Delete vehicle: " + e.getMessage());
            return false;
        }
    }

    public int getTotalVehicles() {
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM vehicle");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("❌ Count vehicles: " + e.getMessage());
        }
        return 0;
    }

    public List<Vehicle> searchVehicles(String kw) {
        List<Vehicle> list = new ArrayList<>();
        String sql = """
                SELECT * FROM vehicle
                WHERE registration_number ILIKE ? OR make ILIKE ? OR model ILIKE ?
                ORDER BY vehicle_id
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            String w = "%" + kw + "%";
            stmt.setString(1, w); stmt.setString(2, w); stmt.setString(3, w);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Search vehicles: " + e.getMessage());
        }
        return list;
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getInt("vehicle_id"),
                rs.getString("registration_number"),
                rs.getString("make"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("color")          != null ? rs.getString("color")          : "",
                rs.getString("chassis_number") != null ? rs.getString("chassis_number") : "",
                rs.getInt("owner_id")
        );
    }
}