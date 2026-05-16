package com.example.classsync.db;

import com.example.classsync.model.Task;
import com.example.classsync.model.TaskStatus;
import com.example.classsync.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {

    public static List<Task> findByGroupId(String groupId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE group_id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return tasks;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) tasks.add(mapRow(rs, conn));
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findByGroupId tasks failed: " + e.getMessage());
        }
        return tasks;
    }

    public static List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return tasks;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) tasks.add(mapRow(rs, conn));
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findAll tasks failed: " + e.getMessage());
        }
        return tasks;
    }

    public static Optional<Task> findById(String id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return Optional.empty();

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Task t = mapRow(rs, conn);
                rs.close();
                stmt.close();
                return Optional.of(t);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findById task failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static boolean save(Task task) {
        String sql = """
                INSERT INTO tasks
                    (id, title, description, group_id, assignee_id,
                     status, deadline, ai_score, submission_file, submission_note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, task.getId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription() != null ? task.getDescription() : "");
            stmt.setString(4, task.getGroupId());
            stmt.setString(5, task.getAssignee() != null ? task.getAssignee().getId() : null);
            stmt.setString(6, task.getStatus().name());
            stmt.setDate(7, task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null);
            stmt.setDouble(8, task.getAiScore());
            stmt.setString(9, task.getSubmissionFile() != null ? task.getSubmissionFile() : "");
            stmt.setString(10, task.getSubmissionNote() != null ? task.getSubmissionNote() : "");
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("save task failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateStatus(String taskId, TaskStatus status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.name());
            stmt.setString(2, taskId);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("updateStatus failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateSubmission(String taskId, String filePath,
                                           String note, double aiScore) {
        String sql = """
                UPDATE tasks
                SET status = 'COMPLETED', submission_file = ?,
                    submission_note = ?, ai_score = ?
                WHERE id = ?
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, filePath);
            stmt.setString(2, note != null ? note : "");
            stmt.setDouble(3, aiScore);
            stmt.setString(4, taskId);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("updateSubmission failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAiScore(String taskId, double score) {
        String sql = "UPDATE tasks SET ai_score = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, score);
            stmt.setString(2, taskId);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("updateAiScore failed: " + e.getMessage());
            return false;
        }
    }

    private static Task mapRow(ResultSet rs, Connection conn) throws SQLException {
        String assigneeId = rs.getString("assignee_id");
        User assignee = null;
        if (assigneeId != null) {
            assignee = UserRepository.findById(assigneeId).orElse(null);
        }

        Date deadlineDate = rs.getDate("deadline");
        LocalDate deadline = deadlineDate != null ? deadlineDate.toLocalDate() : null;

        Task task = new Task(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("description"),
                assignee,
                TaskStatus.valueOf(rs.getString("status")),
                deadline,
                rs.getString("group_id")
        );
        task.setAiScore(rs.getDouble("ai_score"));
        task.setSubmissionFile(rs.getString("submission_file"));
        task.setSubmissionNote(rs.getString("submission_note"));
        return task;
    }
}