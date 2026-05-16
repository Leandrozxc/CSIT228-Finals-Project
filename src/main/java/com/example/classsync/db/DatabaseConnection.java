package com.example.classsync.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection get() {
        try {
            if (connection == null || connection.isClosed()) {
                Properties props = new Properties();
                InputStream in = DatabaseConnection.class
                        .getClassLoader()
                        .getResourceAsStream("db.properties");
                if (in == null) {
                    System.err.println("db.properties not found on classpath.");
                    return null;
                }
                props.load(in);
                connection = DriverManager.getConnection(
                        props.getProperty("db.url"),
                        props.getProperty("db.user"),
                        props.getProperty("db.password")
                );
                System.out.println("DB connected.");
            }
        } catch (Exception e) {
            System.err.println("DB connection failed: " + e.getMessage());
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("DB connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close DB: " + e.getMessage());
        }
    }
}