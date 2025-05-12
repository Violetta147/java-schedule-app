package com.yourcompany.schedule.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/schedule_db";
    private static final String USER = "java";
    private static final String PASSWORD = "Vuongtuenhi15052005!";

    private Connection connection;

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                // If the database doesn't exist yet, connect to MySQL without a specific database
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASSWORD);
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