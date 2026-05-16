package com.example.classsync.db;

import com.example.classsync.model.Evaluation;
import com.example.classsync.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EvaluationRepository {

    public static boolean save(Evaluation eval) {
        String sql = """
                INSERT INTO evaluations
                    (id, evaluator_id, evaluated_id, group_id,
                     effort, reliability, quality, overall, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, eval.getId());
            stmt.setString(2, eval.getEvaluator().getId());
            stmt.setString(3, eval.getEvaluated().getId());
            stmt.setString(4, eval.getGroupId());
            stmt.setInt(5, eval.getEffort());
            stmt.setInt(6, eval.getReliability());
            stmt.setInt(7, eval.getQuality());
            stmt.setInt(8, eval.getOverall());
            stmt.setString(9, eval.getNotes() != null ? eval.getNotes() : "");
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("save evaluation failed: " + e.getMessage());
            return false;
        }
    }

    public static List<Evaluation> findByGroupId(String groupId) {
        List<Evaluation> evals = new ArrayList<>();
        String sql = "SELECT * FROM evaluations WHERE group_id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return evals;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) evals.add(mapRow(rs));
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findByGroupId evaluations failed: " + e.getMessage());
        }
        return evals;
    }

    public static boolean hasEvaluated(String evaluatorId,
                                       String evaluatedId, String groupId) {
        String sql = """
                SELECT COUNT(*) FROM evaluations
                WHERE evaluator_id = ? AND evaluated_id = ? AND group_id = ?
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, evaluatorId);
            stmt.setString(2, evaluatedId);
            stmt.setString(3, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("hasEvaluated failed: " + e.getMessage());
        }
        return false;
    }

    private static Evaluation mapRow(ResultSet rs) throws SQLException {
        User evaluator = UserRepository.findById(rs.getString("evaluator_id")).orElse(null);
        User evaluated = UserRepository.findById(rs.getString("evaluated_id")).orElse(null);
        return new Evaluation(
                rs.getString("id"),
                evaluator,
                evaluated,
                rs.getString("group_id"),
                rs.getInt("effort"),
                rs.getInt("reliability"),
                rs.getInt("quality"),
                rs.getInt("overall"),
                rs.getString("notes")
        );
    }
}