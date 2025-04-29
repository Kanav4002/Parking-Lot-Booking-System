package com.chitkara.parking;

import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private User user;
    private ParkingSlot slot;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;
    private double fee;  // Added fee variable

    public Booking(int id, User user, ParkingSlot slot, LocalDateTime in, LocalDateTime out) {
        this.bookingId = id;
        this.user = user;
        this.slot = slot;
        this.timeIn = in;
        this.timeOut = out;
        this.fee = calculateFee();  // Optional, can implement the calculation logic later
    }

    public int getBookingId() {
        return bookingId;
    }

    public double getFee() {
        return fee;
    }

    private double calculateFee() {
        // If exit time is earlier than entry time, set it to a default value or show an error
        if (timeOut.isBefore(timeIn)) {
            System.out.println("⚠️ Error: Exit time cannot be before entry time.");
            return 0;  // Return 0 to prevent negative fee, or you can set a default fee
        }

        // Calculate the duration in hours
        long duration = java.time.Duration.between(timeIn, timeOut).toHours();

        // Ensure a minimum duration of 1 hour (or whatever minimum you want)
        duration = duration < 1 ? 1 : duration;

        // Calculate the fee (₹20 per hour for example)
        return 20.0 * duration;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    // add inside Booking class
    public User getUser() {
        return this.user;
    }
    public LocalDateTime getTimeIn() {
        return this.timeIn;
    }
    public LocalDateTime getTimeOut() {
        return this.timeOut;
    }
    public void setTimeIn(LocalDateTime timeIn) {
        this.timeIn = timeIn;
        this.fee = calculateFee();
    }
    public void setTimeOut(LocalDateTime timeOut) {
        this.timeOut = timeOut;
        this.fee = calculateFee();
    }
}
