package com.example.vehicleidentification.model;

public class Customer {
    private int    customerId;
    private String userId;     // NULL if no login account
    private String name;
    private String address;
    private String phone;
    private String email;

    public Customer() {}

    public Customer(int customerId, String userId, String name,
                    String address, String phone, String email) {
        this.customerId = customerId;
        this.userId     = userId;
        this.name       = name;
        this.address    = address;
        this.phone      = phone;
        this.email      = email;
    }

    public int    getCustomerId()      { return customerId; }
    public void   setCustomerId(int c) { this.customerId = c; }
    public String getUserId()          { return userId; }
    public void   setUserId(String u)  { this.userId = u; }
    public String getName()            { return name; }
    public void   setName(String n)    { this.name = n; }
    public String getAddress()         { return address; }
    public void   setAddress(String a) { this.address = a; }
    public String getPhone()           { return phone; }
    public void   setPhone(String p)   { this.phone = p; }
    public String getEmail()           { return email; }
    public void   setEmail(String e)   { this.email = e; }

    /** True if this customer has a system login account. */
    public boolean hasLogin() { return userId != null && !userId.isBlank(); }

    /** For display in the "Login Access" column. */
    public String getLoginAccess() { return hasLogin() ? "✔ Yes (" + userId + ")" : "✘ None"; }

    @Override
    public String toString() { return customerId + " - " + name; }
}