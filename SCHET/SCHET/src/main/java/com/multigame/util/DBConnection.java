package com.multigame.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection — Singleton utility class for MySQL connection.
 *
 * Only ONE connection is created for the entire application lifetime.
 * All DAOs call DBConnection.getConnection() to get this shared connection.
 *
 * HOW TO CONFIGURE:
 *   Change DB_URL, USER, and PASSWORD below to match your MySQL setup.
 *   The database name in DB_URL must match what you created (MULTIGAMEDB).
 */
public class DBConnection {

    // ── Connection settings ─────────────────────────────────────────────────
    // useSSL=false        → disable SSL warning on local machines
    // serverTimezone=UTC  → avoid timezone mismatch errors
    private static final String DB_URL   = "jdbc:mysql://localhost:3306/MULTIGAMEDB"
                                         + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "Abhi_2698084w";

    // The single shared connection object
    private static Connection connection;

    // Private constructor — prevents anyone from doing "new DBConnection()"
    private DBConnection() {}

    /**
     * Returns the shared MySQL connection.
     * Creates it on the first call; reuses it on every subsequent call.
     * If the connection was closed, it reopens it automatically.
     *
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException 
    {
        if (connection == null || connection.isClosed()) 
            {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("✔ Database connected successfully.");
             }
        return connection;
    }

    /**
     * Cleanly closes the connection.
     * Called from MainApp.stop() when the user closes the window.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) 
            {
                connection.close();
                System.out.println("✔ Database connection closed.");
            }
        } 
           catch (SQLException e) 
             {
            System.err.println("Error closing connection: " + e.getMessage());
             }
    }
}
