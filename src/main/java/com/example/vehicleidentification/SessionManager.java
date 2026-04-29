package com.example.vehicleidentification;

import com.example.vehicleidentification.model.User;

public class SessionManager {
    private static User loggedInUser;

    public static void setLoggedInUser(User user) { loggedInUser = user; }
    public static User getLoggedInUser()          { return loggedInUser; }
    public static void clearSession()             { loggedInUser = null; }
    public static String getRole() {
        return loggedInUser != null ? loggedInUser.getRole() : "";
    }
    public static String getUserId() {
        return loggedInUser != null ? loggedInUser.getUserId() : "";
    }
}