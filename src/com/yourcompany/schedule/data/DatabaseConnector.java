package com.yourcompany.schedule.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/java";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Try connecting to the target database
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                // Connect without specifying a database
                Connection tempConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASSWORD);
                try (Statement stmt = tempConn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS java");
                }
                tempConn.close();
                // Now connect to the target database
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 