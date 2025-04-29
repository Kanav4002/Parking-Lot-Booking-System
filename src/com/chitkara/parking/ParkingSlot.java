package com.chitkara.parking;

public class ParkingSlot {
    private int slotId;
    private boolean isAvailable;
    private int floor;
    private String type;  // "Car", "Bike", etc.

    public ParkingSlot(int slotId, int floor, String type) {
        this.slotId = slotId;
        this.floor = floor;
        this.type = type;
        this.isAvailable = true;
    }

    public void markBooked()   { isAvailable = false; }
    public void markAvailable(){ isAvailable = true;  }
    // getters/setters omitted

    // ✅ IN ParkingSlot.java
    public boolean isAvailable() { return isAvailable; }
    public int getSlotId() { return slotId; }
    public int getFloor() { return floor; }
    public String getType() { return type; }

}
