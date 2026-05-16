package com.example.classsync.data;

import com.example.classsync.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Single source of truth for all mock data.
 * Replace this with real DAO/service calls when connecting to Supabase.
 */
public class  MockData {

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

    private MockData() { seed(); }

    // ── Seed ──────────────────────────────────────────────────────────────────
    private void seed() {

        // ── Users ──
        User admin  = new User("u0", "System Admin",  "admin@classsync.com",       "admin123", Role.ADMIN,       "#a78bfa", "");
        User prof   = new User("u1", "Prof. Santos",  "prof.santos@classsync.com", "prof123",  Role.INSTRUCTOR,  "#f59e0b", "");
        User alex   = new User("u2", "Alex Johnson",  "alex@classsync.com",        "pass123",  Role.STUDENT,     "#e94560", "CS301-A");
        User sarah  = new User("u3", "Sarah Chen",    "sarah@classsync.com",       "pass123",  Role.STUDENT,     "#60a5fa", "CS301-A");
        User mike   = new User("u4", "Mike Ross",     "mike@classsync.com",        "pass123",  Role.STUDENT,     "#4ade80", "CS301-B");
        users.addAll(List.of(admin, prof, alex, sarah, mike));

        // ── Groups ──
        Group g1 = new Group("g1", "Quantum Builders", "OOP2",    "CS301-A");
        g1.addMember(alex,  true);   // Alex is leader of this group
        g1.addMember(sarah, false);
        g1.addMember(mike,  false);

        Group g2 = new Group("g2", "Data Drifters",   "CS101",   "CS301-B");
        g2.addMember(sarah, true);   // Sarah is leader here (she's member in g1)
        g2.addMember(mike,  false);
        g2.addMember(alex,  false);

        groups.addAll(List.of(g1, g2));

        // ── Tasks ──
        tasks.addAll(List.of(
                new Task("t1", "Database Schema Design",
                        "Design the normalized PostgreSQL schema for the project.",
                        alex, TaskStatus.COMPLETED,  LocalDate.now().minusDays(3), "g1"),
                new Task("t2", "Frontend Integration",
                        "Connect the JavaFX UI components to the backend REST APIs.",
                        sarah, TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(1), "g1"),
                new Task("t3", "User Authentication",
                        "Implement Firebase login and session management.",
                        mike, TaskStatus.PENDING, LocalDate.now().plusDays(5), "g1"),
                new Task("t4", "API Documentation",
                        "Document all REST endpoints using OpenAPI spec.",
                        sarah, TaskStatus.PENDING, LocalDate.now().plusDays(2), "g2"),
                new Task("t5", "Data Cleaning Script",
                        "Write a Python script to clean the dataset.",
                        mike, TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(4), "g2")
        ));

        // ── Evaluations (Sarah already evaluated Alex in g1) ──
        evaluations.add(new Evaluation(
                "e1", sarah, alex, "g1",
                4, 5, 4, 4,
                "Alex is very reliable and leads the group well."
        ));

        // ── Notifications ──
        LocalDateTime now = LocalDateTime.now();
        notifications.addAll(List.of(
                new Notification("n1", alex.getId(),  "g1",
                        "Prof. Santos posted a new task in Quantum Builders: \"User Authentication\"",
                        now.minusHours(1)),
                new Notification("n2", alex.getId(),  "g1",
                        "Sarah Chen completed \"Frontend Integration\" in Quantum Builders",
                        now.minusHours(3)),
                new Notification("n3", sarah.getId(), "g1",
                        "Alex Johnson assigned you to \"Frontend Integration\"",
                        now.minusHours(5)),
                new Notification("n4", sarah.getId(), "g2",
                        "Mike Ross updated task status in Data Drifters",
                        now.minusDays(1)),
                new Notification("n5", mike.getId(),  "g1",
                        "You have a new task: \"User Authentication\" in Quantum Builders",
                        now.minusHours(2)),
                new Notification("n6", mike.getId(),  "g2",
                        "Sarah Chen added a comment in Data Drifters",
                        now.minusDays(1))
        ));
    }

    // ── Auth ──────────────────────────────────────────────────────────────────
    /** Returns the user if email+password match, else empty. */
    public Optional<User> login(String email, String password) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim())
                        && u.getPassword().equals(password))
                .findFirst();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public List<User>         getUsers()         { return users; }
    public List<Group>        getGroups()        { return groups; }
    public List<Task>         getTasks()         { return tasks; }
    public List<Evaluation>   getEvaluations()   { return evaluations; }
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