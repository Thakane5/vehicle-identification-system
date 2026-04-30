package com.example.vehicleidentification.model;

import java.time.LocalDate;

public class CustomerQuery {
    private int       queryId;
    private int       customerId;
    private String userId;
    private int       vehicleId;
    private LocalDate queryDate;
    private String    queryText;
    private String    responseText;
    private String    status;

    public CustomerQuery() {}

    public CustomerQuery(int queryId, int customerId, int vehicleId,
                         LocalDate queryDate, String queryText,
                         String responseText, String status) {
        this.queryId      = queryId;
        this.customerId   = customerId;
        this.vehicleId    = vehicleId;
        this.queryDate    = queryDate;
        this.queryText    = queryText;
        this.responseText = responseText;
        this.status       = status;
    }

    public int       getQueryId()              { return queryId; }
    public void      setQueryId(int q)         { this.queryId = q; }
    public int       getCustomerId()           { return customerId; }
    public void      setCustomerId(int c)      { this.customerId = c; }
    public String getUserId() { return userId; }  // int — was wrongly String
    public void setUserId(String u) { this.userId = u; }
    public int       getVehicleId()            { return vehicleId; }
    public void      setVehicleId(int v)       { this.vehicleId = v; }
    public LocalDate getQueryDate()            { return queryDate; }
    public void      setQueryDate(LocalDate d) { this.queryDate = d; }
    public String    getQueryText()            { return queryText; }
    public void      setQueryText(String t)    { this.queryText = t; }
    public String    getResponseText()         { return responseText; }
    public void      setResponseText(String r) { this.responseText = r; }
    public String    getStatus()               { return status; }
    public void      setStatus(String s)       { this.status = s; }
}