package com.example.okta.sync.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import java.sql.Timestamp;


public class DatabaseService {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/aegies_okta";
    private static final String USER = "root";
    private static final String PASS = "root";

    // ✅ Insert user into DPS_USER table
    public static void insertUser(String id, String login, String firstName, String lastName, String email) {
        String sql = "INSERT IGNORE INTO DPS_USER (ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.setString(2, login);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, email);
            stmt.executeUpdate();

            System.out.println("✅ User inserted: " + login);

        } catch (SQLException e) {
            System.err.println("❌ Failed to insert user: " + login);
            e.printStackTrace();
        }
    }

    // ✅ Insert event into DSS_DPS_EVENT table
    public static void insertEvent(String id, String timestamp, String sessionId, String profileId, String name, String email) {
        String sql = "INSERT INTO DSS_DPS_EVENT (ID, TIMESTAMP, SESSIONID, PROFILEID, NAME, EMAIL) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.setString(2, timestamp);
            stmt.setString(3, sessionId);
            stmt.setString(4, profileId);
            stmt.setString(5, name);
            stmt.setString(6, email);
            stmt.executeUpdate();

            System.out.println("✅ Event inserted: " + id);

        } catch (SQLException e) {
            System.err.println("❌ Failed to insert event: " + id);
            e.printStackTrace();
        }
    }

    // ✅ Check if a user with the given login already exists
    public static boolean userExists(String login) {
        String sql = "SELECT 1 FROM DPS_USER WHERE LOGIN = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if user exists
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to check user existence: " + login);
            e.printStackTrace();
            return false;
        }
    }
    
    // ✅ Get the last sync timestamp from SYNC_META table
    public static LocalDateTime getLastSyncTime() {
        String sql = "SELECT LAST_SYNC_TIMESTAMP FROM SYNC_META WHERE ID = 1";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getTimestamp(1).toLocalDateTime();
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to fetch last sync time");
            e.printStackTrace();
        }
        // Fallback to default sync time
        return LocalDateTime.of(2024, 1, 1, 0, 0);
    }

    // ✅ Update the sync timestamp after successful fetch
    public static void updateLastSyncTime(LocalDateTime now) {
        String sql = "UPDATE SYNC_META SET LAST_SYNC_TIMESTAMP = ? WHERE ID = 1";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(now));
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Failed to update last sync time");
            e.printStackTrace();
        }
    }
}
