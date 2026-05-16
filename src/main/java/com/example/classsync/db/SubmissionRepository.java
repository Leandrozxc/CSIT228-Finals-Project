package com.example.classsync.db;

import java.sql.*;
import java.util.UUID;

public class SubmissionRepository {

    public static boolean save(String taskId, String userId,
                               String filePath, String note) {
        String sql = """
                INSERT INTO submissions
                    (id, task_id, user_id, file_path, note, submitted_at, ai_score, ai_feedback)
                VALUES (?, ?, ?, ?, ?, NOW(), 0, '')
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, taskId);
            stmt.setString(3, userId);
            stmt.setString(4, filePath);
            stmt.setString(5, note != null ? note : "");
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("save submission failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateScore(String taskId, double aiScore, String aiFeedback) {
        String sql = """
                UPDATE submissions SET ai_score = ?, ai_feedback = ?
                WHERE task_id = ?
                ORDER BY submitted_at DESC
                LIMIT 1
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, aiScore);
            stmt.setString(2, aiFeedback != null ? aiFeedback : "");
            stmt.setString(3, taskId);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("updateScore failed: " + e.getMessage());
            return false;
        }
    }
}