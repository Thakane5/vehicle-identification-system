package com.example.vehicleidentification.DataAccessObject;

import com.example.vehicleidentification.database.DBConnection;
import com.example.vehicleidentification.model.InsurancePolicy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsurancePolicyDAO {

    public boolean addPolicy(InsurancePolicy p) {
        String sql = """
                INSERT INTO insurancepolicy (vehicle_id, customer_id, user_id,
                    policy_number, provider_name, coverage_type,
                    start_date, end_date, premium_amount, status)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt   (1, p.getVehicleId());
            stmt.setInt   (2, p.getCustomerId());
            stmt.setString(3, p.getUserId());
            stmt.setString(4, p.getPolicyNumber());
            stmt.setString(5, p.getProviderName());
            stmt.setString(6, p.getCoverageType());
            stmt.setDate  (7, Date.valueOf(p.getStartDate()));
            stmt.setDate  (8, Date.valueOf(p.getEndDate()));
            stmt.setDouble(9, p.getPremiumAmount());
            stmt.setString(10, p.getStatus() != null ? p.getStatus() : "Active");
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Add policy: " + e.getMessage());
            return false;
        }
    }

    public List<InsurancePolicy> getAllPolicies() {
        List<InsurancePolicy> list = new ArrayList<>();
        String sql = """
                SELECT ip.*, v.registration_number
                FROM insurancepolicy ip
                LEFT JOIN vehicle v ON ip.vehicle_id = v.vehicle_id
                ORDER BY ip.policy_id
                """;
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get policies: " + e.getMessage());
        }
        return list;
    }

    public List<InsurancePolicy> getPoliciesByCustomer(int customerId) {
        List<InsurancePolicy> list = new ArrayList<>();
        String sql = """
                SELECT ip.*, v.registration_number
                FROM insurancepolicy ip
                LEFT JOIN vehicle v ON ip.vehicle_id = v.vehicle_id
                WHERE ip.customer_id=?
                ORDER BY ip.policy_id
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("❌ Get policies by customer: " + e.getMessage());
        }
        return list;
    }

    public boolean updatePolicy(InsurancePolicy p) {
        String sql = """
                UPDATE insurancepolicy SET provider_name=?, coverage_type=?,
                    start_date=?, end_date=?, premium_amount=?, status=?
                WHERE policy_id=?
                """;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, p.getProviderName());
            stmt.setString(2, p.getCoverageType());
            stmt.setDate  (3, Date.valueOf(p.getStartDate()));
            stmt.setDate  (4, Date.valueOf(p.getEndDate()));
            stmt.setDouble(5, p.getPremiumAmount());
            stmt.setString(6, p.getStatus());
            stmt.setInt   (7, p.getPolicyId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Update policy: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePolicy(int policyId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM insurancepolicy WHERE policy_id=?");
            stmt.setInt(1, policyId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(" Delete policy: " + e.getMessage());
            return false;
        }
    }

    public void autoExpirePolicies() {
        try {
            Connection conn = DBConnection.getConnection();
            conn.createStatement().executeUpdate("""
                    UPDATE insurancepolicy SET status='Expired'
                    WHERE end_date < CURRENT_DATE AND status='Active'
                    """);
        } catch (SQLException e) {
            System.out.println(" Auto-expire: " + e.getMessage());
        }
    }

    private InsurancePolicy mapRow(ResultSet rs) throws SQLException {
        String reg = "";
        try { reg = rs.getString("registration_number"); } catch (SQLException ignored) {}
        String userId = "";
        try { userId = rs.getString("user_id"); } catch (SQLException ignored) {}
        return new InsurancePolicy(
                rs.getInt("policy_id"),
                rs.getInt("vehicle_id"),
                reg,
                rs.getInt("customer_id"),
                userId,
                rs.getString("policy_number"),
                rs.getString("provider_name"),
                rs.getString("coverage_type") != null ? rs.getString("coverage_type") : "",
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getDouble("premium_amount"),
                rs.getString("status")
        );
    }
}