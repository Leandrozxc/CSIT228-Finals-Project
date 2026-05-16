package com.example.classsync.session;

import com.example.classsync.model.Group;
import com.example.classsync.model.User;

/** Holds the currently logged-in user and any selected context for the session. */
public class Session {
    private static Session instance;
    private User  currentUser;
    private Group selectedGroup;

    private Session() {}

    public static Session get() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public User  getCurrentUser()          { return currentUser; }
    public void  setCurrentUser(User u)    { currentUser = u; }
    public void  clear()                   { currentUser = null; selectedGroup = null; }
    public boolean isLoggedIn()            { return currentUser != null; }

    public Group getSelectedGroup()        { return selectedGroup; }
    public void  setSelectedGroup(Group g) { selectedGroup = g; }
}