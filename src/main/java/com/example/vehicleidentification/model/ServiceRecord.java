package com.example.vehicleidentification.model;

import java.time.LocalDate;

public class ServiceRecord {
    private int       serviceId;
    private int       vehicleId;
    private LocalDate serviceDate;
    private String    serviceType;
    private String    description;
    private double    cost;

    public ServiceRecord() {}

    public ServiceRecord(int serviceId, int vehicleId, LocalDate serviceDate,
                         String serviceType, String description, double cost) {
        this.serviceId   = serviceId;
        this.vehicleId   = vehicleId;
        this.serviceDate = serviceDate;
        this.serviceType = serviceType;
        this.description = description;
        this.cost        = cost;
    }

    public int       getServiceId()                       { return serviceId; }
    public void      setServiceId(int serviceId)          { this.serviceId = serviceId; }
    public int       getVehicleId()                       { return vehicleId; }
    public void      setVehicleId(int vehicleId)          { this.vehicleId = vehicleId; }
    public LocalDate getServiceDate()                     { return serviceDate; }
    public void      setServiceDate(LocalDate serviceDate){ this.serviceDate = serviceDate; }
    public String    getServiceType()                     { return serviceType; }
    public void      setServiceType(String serviceType)   { this.serviceType = serviceType; }
    public String    getDescription()                     { return description; }
    public void      setDescription(String description)   { this.description = description; }
    public double    getCost()                            { return cost; }
    public void      setCost(double cost)                 { this.cost = cost; }
}