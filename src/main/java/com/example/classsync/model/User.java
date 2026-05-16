package com.example.classsync.model;

public class User {
    private final String id;
    private final String name;
    private final String email;
    private final String password;
    private Role   role;
    private final String color;    // avatar color
    private final String section;
    private boolean isActive = true;

    public User(String id, String name, String email, String password,
                Role role, String color, String section) {
        this.id       = id;
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.role     = role;
        this.color    = color;
        this.section  = section;
    }
    public void setActive(boolean active) {isActive = active;}
    public void setRole(Role role) { this.role = role; }
    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public Role   getRole()     { return role; }
    public String getColor()    { return color; }
    public String getSection()  { return section; }
    public boolean isActive() { return isActive; }

    /** Two-letter initials from name */
    public String getInitials() {
        String[] parts = name.trim().split(" ");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        return ("" + parts[0].charAt(0)).toUpperCase();
    }

    @Override public String toString() { return name; }
}