package com.example.vehicleidentification.model;

import java.time.LocalDate;

public class PoliceReport {
    private int       reportId;
    private int       vehicleId;
    private String    vehicleReg;
    private String    userId;      // FK → users.user_id
    private LocalDate reportDate;
    private String    reportType;
    private String    description;
    private String    officerName;
    private String    stationName;
    private String    caseNumber;

    public PoliceReport() {}

    public PoliceReport(int reportId, int vehicleId, String vehicleReg,
                        String userId, LocalDate reportDate, String reportType,
                        String description, String officerName,
                        String stationName, String caseNumber) {
        this.reportId    = reportId;
        this.vehicleId   = vehicleId;
        this.vehicleReg  = vehicleReg;
        this.userId      = userId;
        this.reportDate  = reportDate;
        this.reportType  = reportType;
        this.description = description;
        this.officerName = officerName;
        this.stationName = stationName;
        this.caseNumber  = caseNumber;
    }

    public int       getReportId()                        { return reportId; }
    public void      setReportId(int r)                   { this.reportId = r; }
    public int       getVehicleId()                       { return vehicleId; }
    public void      setVehicleId(int v)                  { this.vehicleId = v; }
    public String    getVehicleReg()                      { return vehicleReg; }
    public void      setVehicleReg(String v)              { this.vehicleReg = v; }
    public String    getUserId()                          { return userId; }
    public void      setUserId(String u)                  { this.userId = u; }
    public LocalDate getReportDate()                      { return reportDate; }
    public void      setReportDate(LocalDate d)           { this.reportDate = d; }
    public String    getReportType()                      { return reportType; }
    public void      setReportType(String t)              { this.reportType = t; }
    public String    getDescription()                     { return description; }
    public void      setDescription(String d)             { this.description = d; }
    public String    getOfficerName()                     { return officerName; }
    public void      setOfficerName(String o)             { this.officerName = o; }
    public String    getStationName()                     { return stationName; }
    public void      setStationName(String s)             { this.stationName = s; }
    public String    getCaseNumber()                      { return caseNumber; }
    public void      setCaseNumber(String c)              { this.caseNumber = c; }
}