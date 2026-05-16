package com.example.classsync.data;

import com.example.classsync.db.*;
import com.example.classsync.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Replaces MockData. All controllers use DataService.get() instead.
 */
public class DataService {

    private static DataService instance;

    private DataService() {}

    public static DataService get() {
        if (instance == null) instance = new DataService();
        return instance;
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    public Optional<User> login(String email, String password) {
        return UserRepository.findByEmailAndPassword(email, password);
    }

    public Optional<User> findUser(String id) {
        return UserRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return UserRepository.findAll();
    }

    // ── Groups ────────────────────────────────────────────────────────────────

    public List<Group> getGroupsForUser(User user) {
        return GroupRepository.findByUserId(user.getId());
    }

    public Optional<Group> findGroup(String groupId) {
        return GroupRepository.findById(groupId);
    }

    public List<Group> getAllGroups() {
        return GroupRepository.findAll();
    }

    // ── Tasks ─────────────────────────────────────────────────────────────────

    public List<Task> getTasksForGroup(String groupId) {
        return TaskRepository.findByGroupId(groupId);
    }

    public List<Task> getTasks() {
        return TaskRepository.findAll();
    }

    public void addTask(Task task) {
        TaskRepository.save(task);
    }

    public void updateTaskStatus(String taskId, TaskStatus status) {
        TaskRepository.updateStatus(taskId, status);
    }

    public void submitTask(String taskId, String userId,
                           String filePath, String note) {
        SubmissionRepository.save(taskId, userId, filePath, note);
        TaskRepository.updateSubmission(taskId, filePath, note, 0);
    }

    public void updateAiScore(String taskId, double score, String feedback) {
        TaskRepository.updateAiScore(taskId, score);
        SubmissionRepository.updateScore(taskId, score, feedback);
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    public List<Notification> getNotificationsForUser(User user) {
        return NotificationRepository.findByUserId(user.getId());
    }

    public void markNotificationRead(String notifId) {
        NotificationRepository.markRead(notifId);
    }

    public void markAllNotificationsRead(String userId) {
        NotificationRepository.markAllRead(userId);
    }

    public void createNotification(String userId, String message,
                                   String description, String groupId) {
        NotificationRepository.create(userId, message, description, groupId);
    }

    // ── Evaluations ───────────────────────────────────────────────────────────

    public boolean hasEvaluated(String evaluatorId,
                                String evaluatedId, String groupId) {
        return EvaluationRepository.hasEvaluated(evaluatorId, evaluatedId, groupId);
    }

    public void saveEvaluation(Evaluation eval) {
        EvaluationRepository.save(eval);
    }

    public List<Evaluation> getEvaluationsForGroup(String groupId) {
        return EvaluationRepository.findByGroupId(groupId);
    }
}