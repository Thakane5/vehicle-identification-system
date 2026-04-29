package com.example.vehicleidentification.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class InsurancePolicy {
    private int       policyId;
    private int       vehicleId;
    private String    vehicleReg;
    private int       customerId;
    private String    userId;
    private String    policyNumber;
    private String    providerName;
    private String    coverageType;
    private LocalDate startDate;
    private LocalDate endDate;
    private double    premiumAmount;
    private String    status;

    public InsurancePolicy() {}

    public InsurancePolicy(int policyId, int vehicleId, String vehicleReg,
                           int customerId, String userId, String policyNumber,
                           String providerName, String coverageType,
                           LocalDate startDate, LocalDate endDate,
                           double premiumAmount, String status) {
        this.policyId      = policyId;
        this.vehicleId     = vehicleId;
        this.vehicleReg    = vehicleReg;
        this.customerId    = customerId;
        this.userId        = userId;
        this.policyNumber  = policyNumber;
        this.providerName  = providerName;
        this.coverageType  = coverageType;
        this.startDate     = startDate;
        this.endDate       = endDate;
        this.premiumAmount = premiumAmount;
        this.status        = status;
    }

    public String getDaysLeft() {
        if (endDate == null) return "—";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        if (days < 0)  return "Expired";
        if (days == 0) return "Expires today!";
        return days + " days";
    }

    public int       getPolicyId()                          { return policyId; }
    public void      setPolicyId(int p)                     { this.policyId = p; }
    public int       getVehicleId()                         { return vehicleId; }
    public void      setVehicleId(int v)                    { this.vehicleId = v; }
    public String    getVehicleReg()                        { return vehicleReg; }
    public void      setVehicleReg(String v)                { this.vehicleReg = v; }
    public int       getCustomerId()                        { return customerId; }
    public void      setCustomerId(int c)                   { this.customerId = c; }
    public String    getUserId()                            { return userId; }
    public void      setUserId(String u)                    { this.userId = u; }
    public String    getPolicyNumber()                      { return policyNumber; }
    public void      setPolicyNumber(String p)              { this.policyNumber = p; }
    public String    getProviderName()                      { return providerName; }
    public void      setProviderName(String p)              { this.providerName = p; }
    public String    getCoverageType()                      { return coverageType; }
    public void      setCoverageType(String c)              { this.coverageType = c; }
    public LocalDate getStartDate()                         { return startDate; }
    public void      setStartDate(LocalDate s)              { this.startDate = s; }
    public LocalDate getEndDate()                           { return endDate; }
    public void      setEndDate(LocalDate e)                { this.endDate = e; }
    public double    getPremiumAmount()                     { return premiumAmount; }
    public void      setPremiumAmount(double p)             { this.premiumAmount = p; }
    public String    getStatus()                            { return status; }
    public void      setStatus(String s)                    { this.status = s; }
}