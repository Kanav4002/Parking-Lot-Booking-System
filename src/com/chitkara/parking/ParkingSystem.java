package com.chitkara.parking;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;

public class ParkingSystem {
    private List<ParkingSlot> slots = new ArrayList<>();
    private Map<Integer, Booking> bookings = new HashMap<>();
    private Waitlist waitlist = new Waitlist();
    private int bookingCounter = 1;

    public ParkingSystem() {
        slots.add(new ParkingSlot(101, 1, "Car"));
        slots.add(new ParkingSlot(102, 1, "Bike"));
        slots.add(new ParkingSlot(103, 2, "Car"));
        slots.add(new ParkingSlot(104, 2, "Bike"));

        loadBookingsFromFile();
        loadWaitlistFromFile();
    }

    private ParkingSlot findSlotById(int slotId) {
        for (ParkingSlot slot : slots) {
            if (slot.getSlotId() == slotId) {
                return slot;
            }
        }
        return null;  // If no matching slot found, return null
    }

    public void viewAvailableSlots() {
        System.out.println("\n--- Available Parking Slots ---");
        boolean found = false;
        for (ParkingSlot slot : slots) {
            if (slot.isAvailable()) {
                found = true;
                System.out.println("Slot ID: " + slot.getSlotId() +
                        ", Floor: " + slot.getFloor() +
                        ", Type: " + slot.getType());
            }
        }
        if (!found) {
            System.out.println("No slots available at the moment.");
        }
    }

    public void bookSlot(User u, LocalDateTime in, LocalDateTime out, String vehicleType, Scanner sc) {
        // Calculate duration
        long durationHours = java.time.Duration.between(in, out).toHours();

        // Filter slots by vehicle type
        List<ParkingSlot> available = slots.stream()
                .filter(s -> s.isAvailable() && s.getType().equalsIgnoreCase(vehicleType))
                .sorted(Comparator.comparingInt(ParkingSlot::getSlotId))
                .toList();

        if (available.isEmpty()) {
            System.out.println("⚠️ No available slots for your vehicle type (" + vehicleType + ").");
            System.out.print("Do you want to join the waitlist? (Y/N): ");
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("Y")) {
                waitlist.addToWaitlist(u);
                System.out.println("✅ You’ve been added to the waitlist.");
            } else {
                System.out.println("❌ Booking canceled. You are not added to waitlist.");
            }
            return;
        }

        ParkingSlot selectedSlot;
        if (durationHours > 3) {
            // Long duration → pick farthest (last slot)
            selectedSlot = available.get(available.size() - 1);
        } else {
            // Short duration → pick nearest (first slot)
            selectedSlot = available.get(0);
        }

        selectedSlot.markBooked();
        // Create a new booking
        Booking booking = new Booking(bookingCounter++, u, selectedSlot, in, out);
        bookings.put(booking.getBookingId(), booking);  // Add the booking to the map

        // Convert the map to a sorted list by vehicle number
        List<Booking> bookingList = new ArrayList<>(bookings.values());
        bookingList.sort(Comparator.comparing(b -> b.getUser().getVehicleNumber()));  // Sort by vehicle number

        // Update bookings list in sorted order (optional - if you need a sorted list for later use)
        System.out.println("Booking ID: " + booking.getBookingId() + " successfully created!");
    }

    public void findSlotByVehicle(String vehicleNumber) {
        // Create a sorted list of bookings by vehicle number
        List<Booking> bookingList = new ArrayList<>(bookings.values());
        bookingList.sort(Comparator.comparing(b -> b.getUser().getVehicleNumber()));  // Sort bookings by vehicle number

        // Iterate through the sorted list and find the booking with the matching vehicle number
        boolean found = false;
        for (Booking b : bookingList) {
            if (b.getUser().getVehicleNumber().equalsIgnoreCase(vehicleNumber)) {
                // If vehicle number matches, print the booking details
                System.out.println("✅ Slot found!");
                System.out.println("Booking ID: " + b.getBookingId());
                System.out.println("Slot ID: " + b.getSlot().getSlotId());
                System.out.println("Entry Time: " + b.getTimeIn());
                System.out.println("Exit Time: " + b.getTimeOut());
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("⚠️ Vehicle not found.");
        }
    }

    public void cancelBooking(int id) {
        Booking b = bookings.remove(id);
        if (b == null) {
            System.out.println("⚠️ Booking ID " + id + " not found.");
            return;
        }

        // 1. Free up the slot
        ParkingSlot slot = b.getSlot();
        slot.markAvailable();
        System.out.println("✅ Booking " + id + " canceled. Slot " + slot.getSlotId() + " is now free.");

        // 2. If someone’s waiting, book them in automatically
        if (!waitlist.isEmpty()) {
            User nextUser = waitlist.removeFromWaitlist();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoHoursLater = now.plusHours(2);

            // ❌ Remove this extra line:
            // ParkingSlot slot = b.getSlot();

            Booking newBooking = new Booking(bookingCounter++, nextUser, slot, now, twoHoursLater);
            bookings.put(newBooking.getBookingId(), newBooking);
            slot.markBooked();

            System.out.println("🔄 Waitlisted user " + nextUser.getDetails() +
                    " has been booked into slot " + slot.getSlotId() +
                    " with Booking ID " + newBooking.getBookingId());
        }
    }

    public void updateBooking(int id, Scanner sc) {
        Booking b = bookings.get(id);
        if (b == null) {
            System.out.println("⚠️ Booking ID " + id + " not found.");
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Show current times
        System.out.println("\n--- Update Booking ID " + id + " ---");
        System.out.println("Current entry time: " + b.getTimeIn().format(fmt));
        System.out.println("Current exit  time: " + b.getTimeOut().format(fmt));
        System.out.println("1. Change vehicle number");
        System.out.println("2. Change booking end time manually");
        System.out.println("3. Mark Early Exit (Leave now)");
        System.out.print("Choose an option: ");
        int choice = sc.nextInt();
        sc.nextLine(); // consume newline

        switch (choice) {
            case 1 -> {
                System.out.print("Enter new vehicle number: ");
                String newVehicle = sc.nextLine();
                b.getUser().setVehicleNumber(newVehicle);
                System.out.println("✅ Vehicle number updated.");
            }
            case 2 -> {
                System.out.print("Enter new exit time (yyyy-MM-dd HH:mm): ");
                String input = sc.nextLine();
                LocalDateTime newOut = LocalDateTime.parse(input, fmt);

                if (newOut.isBefore(b.getTimeIn())) {
                    System.out.println("⚠️ Error: Exit time cannot be before entry time.");
                } else {
                    b.setTimeOut(newOut);
                    System.out.println("✅ Exit time updated. (Fee recalculated: ₹" + b.getFee() + ")");
                }
            }
            case 3 -> {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(b.getTimeIn())) {
                    System.out.println("⚠️ Error: Cannot leave before entry time.");
                    return;
                }
                b.setTimeOut(now); // Update exit time to now
                System.out.println("✅ Early exit marked. Exit time updated to now (Fee remains unchanged: ₹" + b.getFee() + ")");

                // 1. Free up slot immediately
                ParkingSlot slot = b.getSlot();
                slot.markAvailable();

                // 2. Check and assign to waitlist user if any
                if (!waitlist.isEmpty()) {
                    User nextUser = waitlist.removeFromWaitlist();
                    LocalDateTime twoHoursLater = now.plusHours(2);

                    Booking newBooking = new Booking(bookingCounter++, nextUser, slot, now, twoHoursLater);
                    bookings.put(newBooking.getBookingId(), newBooking);
                    slot.markBooked();

                    System.out.println("🔄 Waitlisted user " + nextUser.getDetails() +
                            " has been booked into slot " + slot.getSlotId() +
                            " with Booking ID " + newBooking.getBookingId());
                }
            }
            default -> System.out.println("⚠️ Invalid choice.");
        }
    }

    public void viewAllBookings() {
        if (bookings.isEmpty()) {
            System.out.println("📭 No bookings found.");
            return;
        }

        System.out.println("\n📋 All Bookings:");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Booking b : bookings.values()) {
            System.out.println("---------------------------");
            System.out.println("Booking ID: " + b.getBookingId());
            System.out.println("User: " + b.getUser().getDetails());
            System.out.println("Slot ID: " + b.getSlot().getSlotId() + " | Type: " + b.getSlot().getType());
            System.out.println("Entry Time: " + b.getTimeIn().format(fmt));
            System.out.println("Exit Time: " + b.getTimeOut().format(fmt));
            System.out.println("Fee: ₹" + b.getFee());
        }
    }

    public void viewWaitlist() {
        System.out.println("\n📋 Current Waitlist:");
        if (waitlist.isEmpty()) {
            System.out.println("No users are currently in the waitlist.");
            return;
        }

        int pos = 1;
        for (User u : waitlist.getWaitlist()) {
            System.out.println(pos++ + ". " + u.getDetails());
        }
    }

    public void saveBookingsToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("bookings.txt"));
            for (Booking b : bookings.values()) {
                writer.write(
                        b.getBookingId() + "," +
                                b.getUser().getName() + "," +
                                b.getUser().getVehicleNumber() + "," +
                                b.getUser().getContact() + "," +
                                b.getSlot().getSlotId() + "," +
                                b.getTimeIn() + "," +
                                b.getTimeOut() + "," +
                                b.getFee()
                );
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("⚠️ Error saving bookings: " + e.getMessage());
        }
    }

    public void saveWaitlistToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("waitlist.txt"));
            for (User u : waitlist.getWaitlist()) {
                writer.write(
                        u.getName() + "," +
                                u.getVehicleNumber() + "," +
                                u.getContact()
                );
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("⚠️ Error saving waitlist: " + e.getMessage());
        }
    }

    public void loadBookingsFromFile() {
        File file = new File("bookings.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
                return;
            }
            int count = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split(",");
                    if (parts.length < 8) {
                        continue; // Skip malformed lines
                    }

                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String vehicleNumber = parts[2].trim();
                    String contact = parts[3].trim();
                    int slotId = Integer.parseInt(parts[4].trim());
                    LocalDateTime timeIn  = LocalDateTime.parse(parts[5].trim());
                    LocalDateTime timeOut = LocalDateTime.parse(parts[6].trim());
                    double fee = Double.parseDouble(parts[7].trim());

                    User user = new User(name, vehicleNumber, contact);
                    ParkingSlot slot = findSlotById(slotId);
                    if (slot != null) {
                        slot.markBooked();
                        Booking booking = new Booking(id, user, slot, timeIn, timeOut);
                        bookings.put(id, booking);
                        count++;
                        bookingCounter = Math.max(bookingCounter, id + 1);
                    }
                }
            }
            System.out.println("✅ Loaded " + count + " bookings.");
        } catch (IOException e) {
            System.out.println("⚠️ Error loading bookings: " + e.getMessage());
        }
    }

    public void loadWaitlistFromFile() {
        File file = new File("waitlist.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
                return;
            }
            int count = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split(",");
                    if (parts.length < 3) {
                        continue; // Skip malformed lines
                    }
                    String name = parts[0].trim();
                    String vehicleNumber = parts[1].trim();
                    String contact = parts[2].trim();

                    User user = new User(name, vehicleNumber, contact);
                    waitlist.addToWaitlist(user);
                    count++;
                }
            }
            System.out.println("✅ Loaded " + count + " waitlist users.");
        } catch (IOException e) {
            System.out.println("⚠️ Error loading waitlist: " + e.getMessage());
        }
    }
}
