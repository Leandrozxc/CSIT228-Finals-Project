package com.example.classsync.db;

import com.example.classsync.model.Role;
import com.example.classsync.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public static Optional<User> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return Optional.empty();

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapRow(rs);
                // Plain password check for now — BCrypt in Phase 3
                if (user.getPassword().equals(password)) {
                    return Optional.of(user);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Login query failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return Optional.empty();

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapRow(rs);
                rs.close();
                stmt.close();
                return Optional.of(user);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findById failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try {
            Connection conn = DatabaseConnection.get();
            if (conn == null) return users;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) users.add(mapRow(rs));
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("findAll users failed: " + e.getMessage());
        }
        return users;
    }

    public static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role")),
                rs.getString("color"),
                rs.getString("section")
        );
    }
}