package com.example.vehicleidentification.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Exact Supabase pooler connection details
    private static final String HOST     = "aws-1-eu-central-1.pooler.supabase.com";
    private static final String PORT     = "6543";
    private static final String DATABASE = "postgres";
    private static final String USER     = "postgres.pkjxjwsvibpofqpfnrrn";
    private static final String PASSWORD = "veronica@123.sello";

    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?sslmode=require";

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connected to Supabase pooler successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ PostgreSQL driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Supabase connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}