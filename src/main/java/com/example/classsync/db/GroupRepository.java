package com.example.classsync.db;

import com.example.classsync.model.Group;
import com.example.classsync.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupRepository {

    public static List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT * FROM groups_tbl";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return groups;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Group g = mapRow(rs);
                loadMembers(g, conn);
                groups.add(g);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findAll groups failed: " + e.getMessage());
        }
        return groups;
    }

    public static Optional<Group> findById(String id) {
        String sql = "SELECT * FROM groups_tbl WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return Optional.empty();

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Group g = mapRow(rs);
                loadMembers(g, conn);
                rs.close();
                stmt.close();
                return Optional.of(g);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findById group failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static List<Group> findByUserId(String userId) {
        List<Group> groups = new ArrayList<>();
        String sql = """
                SELECT g.* FROM groups_tbl g
                JOIN group_members gm ON g.id = gm.group_id
                WHERE gm.user_id = ?
                """;
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return groups;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Group g = mapRow(rs);
                loadMembers(g, conn);
                groups.add(g);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findByUserId groups failed: " + e.getMessage());
        }
        return groups;
    }

    private static void loadMembers(Group group, Connection conn) throws SQLException {
        String sql = """
                SELECT u.*, gm.is_leader
                FROM users u
                JOIN group_members gm ON u.id = gm.user_id
                WHERE gm.group_id = ?
                """;
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, group.getId());
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            User user = UserRepository.mapRow(rs);
            boolean isLeader = rs.getBoolean("is_leader");
            group.addMember(user, isLeader);
        }
        rs.close();
        stmt.close();
    }

    private static Group mapRow(ResultSet rs) throws SQLException {
        return new Group(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("course"),
                rs.getString("section")
        );
    }
}