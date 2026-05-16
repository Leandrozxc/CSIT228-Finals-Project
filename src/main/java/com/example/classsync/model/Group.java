package com.example.classsync.model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private final String id;
    private String name;
    private String course;
    private String section;

    // Per-group membership with role
    public record Member(User user, boolean isLeader) {}

    private final List<Member> members = new ArrayList<>();

    public Group(String id, String name, String course, String section) {
        this.id      = id;
        this.name    = name;
        this.course  = course;
        this.section = section;
    }

    public void addMember(User user, boolean isLeader) {
        members.add(new Member(user, isLeader));
    }

    public boolean isLeader(User user) {
        return members.stream()
                .anyMatch(m -> m.user().getId().equals(user.getId()) && m.isLeader());
    }

    public boolean hasMember(User user) {
        return members.stream()
                .anyMatch(m -> m.user().getId().equals(user.getId()));
    }

    public List<User> getUsers() {
        return members.stream().map(Member::user).toList();
    }

    public List<Member> getMembers()   { return members; }
    public String getId()              { return id; }
    public String getName()            { return name; }
    public void   setName(String n)    { this.name = n; }
    public String getCourse()          { return course; }
    public void   setCourse(String c)  { this.course = c; }
    public String getSection()         { return section; }
    public void   setSection(String s) { this.section = s; }
}