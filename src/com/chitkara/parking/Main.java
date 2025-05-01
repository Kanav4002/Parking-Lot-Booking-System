package com.chitkara.parking;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ParkingSystem ps = new ParkingSystem();
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Parking Lot Menu ---");
            System.out.println("1. View Available Slots");
            System.out.println("2. Book a Slot");
            System.out.println("3. Cancel Booking");
            System.out.println("4. Update Booking");
            System.out.println("5. View All Bookings");
            System.out.println("6. Find Slot by Vehicle Number");
            System.out.println("7. View Waitlist");
            System.out.println("8. Exit");
            System.out.println("9. Check Total Fare"); // ✅ NEW FEATURE
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            switch (choice) {
                case 1 -> ps.viewAvailableSlots();
                case 2 -> {
                    sc.nextLine();  // Consume newline
                    System.out.print("Enter your name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter vehicle type (Car/Bike): ");
                    String type = sc.nextLine().trim();
                    System.out.print("Enter vehicle number: ");
                    String vehicle = sc.nextLine();
                    System.out.print("Enter contact: ");
                    String contact = sc.nextLine();

                    LocalDateTime in = LocalDateTime.now();
                    LocalDateTime out = in.plusHours(2);

                    User u = new User(name, vehicle, contact);
                    ps.bookSlot(u, in, out, type, sc);
                    ps.saveBookingsToFile();
                    ps.saveWaitlistToFile();
                }
                case 3 -> {
                    System.out.print("Enter Booking ID to cancel: ");
                    int cancelId = sc.nextInt();
                    ps.cancelBooking(cancelId);
                    ps.saveBookingsToFile();
                    ps.saveWaitlistToFile();
                }
                case 4 -> {
                    System.out.print("Enter Booking ID to update: ");
                    int updId = sc.nextInt();
                    sc.nextLine();  // consume newline
                    ps.updateBooking(updId, sc);
                    ps.saveBookingsToFile();
                    ps.saveWaitlistToFile();
                }
                case 5 -> ps.viewAllBookings();
                case 6 -> {
                    sc.nextLine();  // Consume newline
                    System.out.print("Enter your vehicle number: ");
                    String vehicleNumber = sc.nextLine();
                    ps.findSlotByVehicle(vehicleNumber);
                }
                case 7 -> ps.viewWaitlist();
                case 8 -> {
                    System.out.println("Goodbye!");
                    ps.saveBookingsToFile();
                    ps.saveWaitlistToFile();
                    sc.close();
                    return;
                }
                case 9 -> { // ✅ NEW CASE
                    sc.nextLine();  // Consume newline
                    System.out.print("Enter vehicle number: ");
                    String vehicleNumber = sc.nextLine();
                    ps.checkFareByVehicleNumber(vehicleNumber);
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
