package com.chitkara.parking;

public class User {
    private String name;
    private String vehicleNumber;
    private String contact;

    public User(String name, String vehicleNumber, String contact) {
        this.name = name;
        this.vehicleNumber = vehicleNumber;
        this.contact = contact;
    }

    public String getDetails() {
        // TODO: return formatted user info
        return name + " (" + vehicleNumber + ")";
    }

    // add inside User class
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
    public String getVehicleNumber() {
        return this.vehicleNumber;
    }

    // ✅ Add these getter methods:
    public String getName() {
        return name;
    }


    public String getContact() {
        return contact;
    }
}
