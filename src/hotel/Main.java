package hotel;

import hotel.service.HotelSystem;
import hotel.service.HotelServer;

public class Main {
    public static void main(String[] args) {
        try {
            HotelSystem system = new HotelSystem("hotel_data.json");
            HotelServer server = new HotelServer(8080, system);
            server.start();

            System.out.println("===============================================");
            System.out.println("  HOTEL RESERVATION SYSTEM ACTIVE");
            System.out.println("  Server Port: 8080");
            System.out.println("  Website URL: http://localhost:8080");
            System.out.println("  Press Ctrl+C in terminal to stop the server.");
            System.out.println("===============================================");

        } catch (Exception e) {
            System.err.println("Failed to start the Hotel Reservation Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
