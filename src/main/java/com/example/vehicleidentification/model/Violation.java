package com.example.vehicleidentification.model;

import java.time.LocalDate;

public class Violation {
    private int       violationId;
    private int       vehicleId;
    private String    vehicleReg;   // for display
    private LocalDate violationDate;
    private String    violationType;
    private String    description;
    private double    fineAmount;
    private String    status;       // Paid / Unpaid
    private String    officerName;
    private String    location;

    public Violation() {}

    public Violation(int violationId, int vehicleId, String vehicleReg,
                     LocalDate violationDate, String violationType,
                     String description, double fineAmount, String status,
                     String officerName, String location) {
        this.violationId   = violationId;
        this.vehicleId     = vehicleId;
        this.vehicleReg    = vehicleReg;
        this.violationDate = violationDate;
        this.violationType = violationType;
        this.description   = description;
        this.fineAmount    = fineAmount;
        this.status        = status;
        this.officerName   = officerName;
        this.location      = location;
    }

    public int       getViolationId()                         { return violationId; }
    public void      setViolationId(int v)                    { this.violationId = v; }
    public int       getVehicleId()                           { return vehicleId; }
    public void      setVehicleId(int v)                      { this.vehicleId = v; }
    public String    getVehicleReg()                          { return vehicleReg; }
    public void      setVehicleReg(String v)                  { this.vehicleReg = v; }
    public LocalDate getViolationDate()                       { return violationDate; }
    public void      setViolationDate(LocalDate v)            { this.violationDate = v; }
    public String    getViolationType()                       { return violationType; }
    public void      setViolationType(String v)               { this.violationType = v; }
    public String    getDescription()                         { return description; }
    public void      setDescription(String d)                 { this.description = d; }
    public double    getFineAmount()                          { return fineAmount; }
    public void      setFineAmount(double f)                  { this.fineAmount = f; }
    public String    getStatus()                              { return status; }
    public void      setStatus(String s)                      { this.status = s; }
    public String    getOfficerName()                         { return officerName; }
    public void      setOfficerName(String o)                 { this.officerName = o; }
    public String    getLocation()                            { return location; }
    public void      setLocation(String l)                    { this.location = l; }
}