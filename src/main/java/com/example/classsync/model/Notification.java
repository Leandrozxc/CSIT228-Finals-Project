package com.example.classsync.model;

import java.time.LocalDateTime;

public class Notification {
    private final String        id;
    private final String        targetUserId;  // who receives this
    private final String        groupId;       // related group (nullable)
    private final String        message;
    private final LocalDateTime timestamp;
    private boolean             read;

    public Notification(String id, String targetUserId, String groupId,
                        String message, LocalDateTime timestamp) {
        this.id           = id;
        this.targetUserId = targetUserId;
        this.groupId      = groupId;
        this.message      = message;
        this.timestamp    = timestamp;
        this.read         = false;
    }

    public String        getId()           { return id; }
    public String        getTargetUserId() { return targetUserId; }
    public String        getGroupId()      { return groupId; }
    public String        getMessage()      { return message; }
    public LocalDateTime getTimestamp()    { return timestamp; }
    public boolean       isRead()          { return read; }
    public void          markRead()        { this.read = true; }
}