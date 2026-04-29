package com.example.vehicleidentification.model;

public class User {
    private String userId;    // VARCHAR PK e.g. ADM001
    private String username;
    private String password;
    private String role;
    private String email;
    private String phone;

    public User() {}

    public User(String userId, String username, String password, String role) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    public User(String userId, String username, String password,
                String role, String email, String phone) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.role     = role;
        this.email    = email;
        this.phone    = phone;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public String getUserId()                 { return userId; }
    public void   setUserId(String userId)    { this.userId = userId; }
    public String getUsername()               { return username; }
    public void   setUsername(String u)       { this.username = u; }
    public String getPassword()               { return password; }
    public void   setPassword(String p)       { this.password = p; }
    public String getRole()                   { return role; }
    public void   setRole(String role)        { this.role = role; }
    public String getEmail()                  { return email; }
    public void   setEmail(String email)      { this.email = email; }
    public String getPhone()                  { return phone; }
    public void   setPhone(String phone)      { this.phone = phone; }

    // Keep getCustomId() as alias so existing code works
    public String getCustomId()               { return userId; }
    public void   setCustomId(String id)      { this.userId = id; }

    // Keep int getUserId for compatibility — returns 0 (use getCustomId() instead)
    public int getIntUserId()                 { return 0; }
}