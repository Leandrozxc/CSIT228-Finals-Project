package com.example.classsync.data;

import com.example.classsync.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Combined Single Source of Truth for Student, Staff, and Admin data.
 */
public class MockData {

    private static MockData instance;
    public static MockData get() {
        if (instance == null) instance = new MockData();
        return instance;
    }

    // ── Raw collections ───────────────────────────────────────────────────────
    private final List<User>         users         = new ArrayList<>();
    private final List<Group>        groups        = new ArrayList<>();
    private final List<Task>         tasks         = new ArrayList<>();
    private final List<Evaluation>   evaluations   = new ArrayList<>();
    private final List<Notification> notifications = new ArrayList<>();

    // ── Admin Specifics ───────────────────────────────────────────────────────
    private final List<AuditEntry> auditLogs = new ArrayList<>();
    private String geminiApiKey = "AIzaSyB_FLASH_EXAMPLE_KEY_123";
    private String aiSystemPrompt = "Analyze student work for logic and detect potential AI patterns or high similarity.";

    private MockData() { seed(); }

    private void seed() {
        // ── Users ──
        User admin = new User("u0", "System Admin", "admin@classsync.com", "admin123", Role.ADMIN, "#a78bfa", "");
        User prof  = new User("u1", "Prof. Santos", "prof.santos@classsync.com", "prof123", Role.INSTRUCTOR, "#f59e0b", "");
        User alex  = new User("u2", "Alex Johnson", "alex@classsync.com", "pass123", Role.STUDENT, "#e94560", "CS301-A");
        User sarah = new User("u3", "Sarah Chen", "sarah@classsync.com", "pass123", Role.STUDENT, "#60a5fa", "CS301-A");
        User mike  = new User("u4", "Mike Ross", "mike@classsync.com", "pass123", Role.STUDENT, "#4ade80", "CS301-B");
        users.addAll(List.of(admin, prof, alex, sarah, mike));

        // ── Groups ──
        Group g1 = new Group("g1", "Quantum Builders", "OOP2", "CS301-A");
        g1.addMember(alex, true);
        g1.addMember(sarah, false);
        g1.addMember(mike, false);

        Group g2 = new Group("g2", "Data Drifters", "CS101", "CS301-B");
        g2.addMember(sarah, true);
        g2.addMember(mike, false);
        g2.addMember(alex, false);
        groups.addAll(List.of(g1, g2));

        // ── Tasks ──
        tasks.addAll(List.of(
                new Task("t1", "Database Schema", "Design PostgreSQL schema.", alex, TaskStatus.COMPLETED, LocalDate.now().minusDays(3), "g1"),
                new Task("t2", "UI Integration", "Connect components.", sarah, TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(1), "g1"),
                new Task("t4", "Documentation", "Document all REST endpoints.", sarah, TaskStatus.PENDING, LocalDate.now().plusDays(2), "g2")
        ));

        // ── Audit Logs ──
        log("System", "BOOT", "ClassSync System Started Successfully", false);
    }

    // ── ADMIN & SYSTEM METHODS ────────────────────────────────────────────────
    public void log(String user, String action, String details, boolean flagged) {
        auditLogs.add(new AuditEntry(user, action, details, flagged));
    }
    public List<AuditEntry> getAuditLogs() { return auditLogs; }

    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String key) { this.geminiApiKey = key; }

    public String getAiSystemPrompt() { return aiSystemPrompt; }
    public void setAiSystemPrompt(String prompt) { this.aiSystemPrompt = prompt; }

    public void deactivateUser(String userId) {
        users.stream().filter(u -> u.getId().equals(userId)).findFirst().ifPresent(u -> u.setActive(false));
    }

    public void updateUserRole(String userId, Role newRole) {
        users.stream().filter(u -> u.getId().equals(userId)).findFirst().ifPresent(u -> u.setRole(newRole));
    }

    // ── REPAIRED STUDENT & EVALUATION ACCESSORS ──────────────────────────────
    public Optional<User> login(String email, String password) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim()) && u.getPassword().equals(password))
                .findFirst();
    }

    public List<User> getUsers()         { return users; }
    public List<Group> getGroups()        { return groups; }
    public List<Task> getTasks()         { return tasks; }
    public List<Evaluation> getEvaluations() { return evaluations; }
    public List<Notification> getNotifications() { return notifications; }

    public List<Group> getGroupsForUser(User u) {
        return groups.stream().filter(g -> g.hasMember(u)).toList();
    }

    public List<Task> getTasksForGroup(String groupId) {
        return tasks.stream().filter(t -> t.getGroupId().equals(groupId)).toList();
    }

    public List<Notification> getNotificationsForUser(User u) {
        return notifications.stream()
                .filter(n -> n.getTargetUserId().equals(u.getId()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList();
    }

    public boolean hasEvaluated(User evaluator, User evaluated, String groupId) {
        return evaluations.stream().anyMatch(e ->
                e.getEvaluator().getId().equals(evaluator.getId()) &&
                        e.getEvaluated().getId().equals(evaluated.getId()) &&
                        e.getGroupId().equals(groupId));
    }

    public void addEvaluation(Evaluation ev) { evaluations.add(ev); }
    public void addTask(Task t)              { tasks.add(t); }
    public void addGroup(Group g)            { groups.add(g); }

    public Group findGroup(String id) {
        return groups.stream().filter(g -> g.getId().equals(id)).findFirst().orElse(null);
    }

    public List<User> getStudents() {
        return users.stream().filter(u -> u.getRole() == Role.STUDENT).toList();
    }
}