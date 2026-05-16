package com.example.classsync.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SubmissionRepository {

    // Save a submission to the DB
    public static boolean save(
            String taskId,
            String userId,
            String filePath,
            String note) {

        String sql = """
                INSERT INTO submissions
                    (id, task_id, user_id, file_path, note, submitted_at, ai_score, ai_feedback)
                VALUES
                    (?, ?, ?, ?, ?, NOW(), 0, '')
                """;

        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) {
                System.err.println("No DB connection — submission not saved.");
                return false;
            }

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
            System.err.println("Failed to save submission: " + e.getMessage());
            return false;
        }
    }

    // Update AI score + feedback after grading (Phase 3)
    public static boolean updateScore(
            String taskId,
            double aiScore,
            String aiFeedback) {

        String sql = """
                UPDATE submissions
                SET ai_score = ?, ai_feedback = ?
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
            System.err.println("Failed to update score: " + e.getMessage());
            return false;
        }
    }

    // Also update task status + ai_score in tasks table
    public static boolean updateTaskStatus(String taskId, double aiScore) {
        String sql = """
                UPDATE tasks
                SET status = 'COMPLETED', ai_score = ?
                WHERE id = ?
                """;

        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, aiScore);
            stmt.setString(2, taskId);
            stmt.executeUpdate();
            stmt.close();
            return true;

        } catch (SQLException e) {
            System.err.println("Failed to update task status: " + e.getMessage());
            return false;
        }
    }
}