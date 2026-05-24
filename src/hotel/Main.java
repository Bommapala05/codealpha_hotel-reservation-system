package hotel;

import hotel.service.HotelSystem;
import hotel.service.HotelServer;

public class Main {
    public static void main(String[] args) {
        try {
            String portEnv = System.getenv("PORT");
            int port = (portEnv != null && !portEnv.isEmpty()) ? Integer.parseInt(portEnv) : 8080;
            HotelSystem system = new HotelSystem("hotel_data.json");
            HotelServer server = new HotelServer(port, system);
            server.start();

            System.out.println("===============================================");
            System.out.println("  HOTEL RESERVATION SYSTEM ACTIVE");
            System.out.println("  Server Port: " + port);
            System.out.println("  Website URL: http://localhost:" + port);
            System.out.println("  Press Ctrl+C in terminal to stop the server.");
            System.out.println("===============================================");

        } catch (Exception e) {
            System.err.println("Failed to start the Hotel Reservation Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
