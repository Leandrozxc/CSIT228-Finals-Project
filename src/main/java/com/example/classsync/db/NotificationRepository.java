package com.example.classsync.db;

import com.example.classsync.model.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationRepository {

    public static List<Notification> findByUserId(String userId) {
        List<Notification> notifs = new ArrayList<>();
        String sql = """
                SELECT * FROM notifications
                WHERE user_id = ?
                ORDER BY timestamp DESC
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return notifs;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) notifs.add(mapRow(rs));
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findByUserId notifications failed: " + e.getMessage());
        }
        return notifs;
    }

    public static boolean save(Notification n) {
        String sql = """
                INSERT INTO notifications
                    (id, user_id, message, description, group_id, is_read, timestamp)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, n.getId());
            stmt.setString(2, n.getTargetUserId());
            stmt.setString(3, n.getMessage());
            stmt.setString(4, n.getDescription() != null ? n.getDescription() : "");
            stmt.setString(5, n.getGroupId());
            stmt.setBoolean(6, n.isRead());
            stmt.setTimestamp(7, Timestamp.valueOf(n.getTimestamp()));
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("save notification failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean markRead(String notifId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, notifId);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("markRead failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean markAllRead(String userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("markAllRead failed: " + e.getMessage());
            return false;
        }
    }

    public static Notification create(String userId, String message,
                                      String description, String groupId) {
        Notification n = new Notification(
                UUID.randomUUID().toString(),
                userId, groupId, message,
                LocalDateTime.now()
        );
        n.setDescription(description);
        save(n);
        return n;
    }

    private static Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification(
                rs.getString("id"),
                rs.getString("user_id"),
                rs.getString("group_id"),
                rs.getString("message"),
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
        n.setDescription(rs.getString("description"));
        if (rs.getBoolean("is_read")) n.markRead();
        return n;
    }
}