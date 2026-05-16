package com.example.classsync.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditEntry {
    private final LocalDateTime timestamp;
    private final String user;
    private final String action;
    private final String details;
    private final boolean isFlagged; // Used for Plagiarism/AI flags

    public AuditEntry(String user, String action, String details, boolean isFlagged) {
        this.timestamp = LocalDateTime.now();
        this.user = user;
        this.action = action;
        this.details = details;
        this.isFlagged = isFlagged;
    }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm:ss"));
    }

    public String getUser() { return user; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public boolean isFlagged() { return isFlagged; }
}