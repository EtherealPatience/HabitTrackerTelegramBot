package org.example.config;

import java.sql.*;

public class DatabaseConfig {
    private static final BotConfig CONFIG = new BotConfig();

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            return DriverManager.getConnection("jdbc:ucanaccess://" + CONFIG.getDbPath());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found", e);
        }
    }
}
