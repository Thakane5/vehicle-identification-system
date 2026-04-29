package com.example.vehicleidentification.model;

public class Vehicle {
    private int    vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private int    year;
    private String color;
    private String chassisNumber;
    private int    ownerId;    // FK → customer.customer_id (integer)

    public Vehicle() {}

    public Vehicle(int vehicleId, String registrationNumber, String make,
                   String model, int year, String color,
                   String chassisNumber, int ownerId) {
        this.vehicleId          = vehicleId;
        this.registrationNumber = registrationNumber;
        this.make               = make;
        this.model              = model;
        this.year               = year;
        this.color              = color;
        this.chassisNumber      = chassisNumber;
        this.ownerId            = ownerId;
    }

    public int    getVehicleId()                          { return vehicleId; }
    public void   setVehicleId(int v)                     { this.vehicleId = v; }
    public String getRegistrationNumber()                 { return registrationNumber; }
    public void   setRegistrationNumber(String r)         { this.registrationNumber = r; }
    public String getMake()                               { return make; }
    public void   setMake(String make)                    { this.make = make; }
    public String getModel()                              { return model; }
    public void   setModel(String model)                  { this.model = model; }
    public int    getYear()                               { return year; }
    public void   setYear(int year)                       { this.year = year; }
    public String getColor()                              { return color; }
    public void   setColor(String color)                  { this.color = color; }
    public String getChassisNumber()                      { return chassisNumber; }
    public void   setChassisNumber(String c)              { this.chassisNumber = c; }
    public int    getOwnerId()                            { return ownerId; }
    public void   setOwnerId(int ownerId)                 { this.ownerId = ownerId; }

    @Override
    public String toString() {
        return registrationNumber + " — " + make + " " + model + " (" + year + ")";
    }
}