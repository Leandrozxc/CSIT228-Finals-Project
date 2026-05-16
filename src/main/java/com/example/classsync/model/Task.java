package com.example.classsync.model;

import java.time.LocalDate;

public class Task {
    private final String id;
    private String     title;
    private String     description;
    private User       assignee;      // nullable
    private TaskStatus status;
    private LocalDate  deadline;
    private final String groupId;

    public Task(String id, String title, String description,
                User assignee, TaskStatus status, LocalDate deadline, String groupId) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.assignee    = assignee;
        this.status      = status;
        this.deadline    = deadline;
        this.groupId     = groupId;
    }

    public String     getId()                    { return id; }
    public String     getTitle()                 { return title; }
    public void       setTitle(String t)         { this.title = t; }
    public String     getDescription()           { return description; }
    public void       setDescription(String d)   { this.description = d; }
    public User       getAssignee()              { return assignee; }
    public void       setAssignee(User u)        { this.assignee = u; }
    public TaskStatus getStatus()                { return status; }
    public void       setStatus(TaskStatus s)    { this.status = s; }
    public LocalDate  getDeadline()              { return deadline; }
    public void       setDeadline(LocalDate d)   { this.deadline = d; }
    public String     getGroupId()               { return groupId; }
}