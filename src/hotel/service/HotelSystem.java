package hotel.service;

import hotel.model.Room;
import hotel.model.Reservation;
import hotel.model.User;
import hotel.utils.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HotelSystem {
    private List<Room> rooms;
    private List<Reservation> reservations;
    private List<User> users;
    private String databasePath;

    public HotelSystem(String databasePath) {
        this.databasePath = databasePath;
        this.rooms = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.users = new ArrayList<>();
        load();
    }

    public synchronized void load() {
        DatabaseManager.loadData(databasePath, rooms, reservations, users);
    }

    public synchronized void save() {
        DatabaseManager.saveData(databasePath, rooms, reservations, users);
    }

    public synchronized List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    public synchronized List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }

    public synchronized List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public synchronized List<Room> searchAvailableRooms(String category, String checkIn, String checkOut) {
        List<Room> availableRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (!category.equalsIgnoreCase("All") && !room.getCategory().equalsIgnoreCase(category)) {
                continue;
            }
            if (isRoomAvailable(room.getRoomNumber(), checkIn, checkOut)) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    public synchronized boolean isRoomAvailable(String roomNumber, String checkIn, String checkOut) {
        for (Reservation res : reservations) {
            if (res.getRoomNumber().equals(roomNumber)) {
                if (datesOverlap(res.getCheckInDate(), res.getCheckOutDate(), checkIn, checkOut)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean datesOverlap(String start1, String end1, String start2, String end2) {
        return start1.compareTo(end2) < 0 && start2.compareTo(end1) < 0;
    }

    public synchronized Reservation bookRoom(String roomNumber, String guestName, String checkInDate, String checkOutDate) {
        Room room = null;
        for (Room r : rooms) {
            if (r.getRoomNumber().equals(roomNumber)) {
                room = r;
                break;
            }
        }
        if (room == null) {
            return null;
        }
        if (!isRoomAvailable(roomNumber, checkInDate, checkOutDate)) {
            return null;
        }

        double days = calculateDays(checkInDate, checkOutDate);
        if (days <= 0) {
            days = 1;
        }
        double totalPrice = room.getPrice() * days;

        String id = "RSV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Reservation reservation = new Reservation(id, roomNumber, guestName, checkInDate, checkOutDate, totalPrice);
        reservations.add(reservation);
        
        save();
        return reservation;
    }

    public synchronized boolean cancelReservation(String id) {
        Reservation target = null;
        for (Reservation res : reservations) {
            if (res.getId().equalsIgnoreCase(id)) {
                target = res;
                break;
            }
        }
        if (target != null) {
            reservations.remove(target);
            save();
            return true;
        }
        return false;
    }

    public synchronized String registerUser(String username, String password, String email) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return null;
            }
        }
        users.add(new User(username, password, email));
        save();
        return hotel.utils.EmailService.sendRegistrationEmail(email, username);
    }

    public synchronized User authenticateUser(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    private double calculateDays(String start, String end) {
        try {
            java.time.LocalDate d1 = java.time.LocalDate.parse(start);
            java.time.LocalDate d2 = java.time.LocalDate.parse(end);
            return java.time.temporal.ChronoUnit.DAYS.between(d1, d2);
        } catch (Exception e) {
            return 1;
        }
    }
}
