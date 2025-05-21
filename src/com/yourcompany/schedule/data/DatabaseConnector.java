package com.yourcompany.schedule.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    // Thay đổi cổng và tên database nếu cần
    private static final String BASE_URL = "jdbc:mysql://localhost:3307/";
    private static final String DEFAULT_DB_NAME = "schedule_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Connection getConnection(String dbName) throws SQLException {
        String targetDbName = (dbName == null || dbName.trim().isEmpty()) ? DEFAULT_DB_NAME : dbName.trim();
        String connectionUrl = BASE_URL + targetDbName + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"; // Thêm các tham số cần thiết
        
        return DriverManager.getConnection(connectionUrl, USER, PASSWORD);
    }

    public Connection getServerConnection() throws SQLException {
        String connectionUrl = BASE_URL + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        return DriverManager.getConnection(connectionUrl, USER, PASSWORD);
    }
}