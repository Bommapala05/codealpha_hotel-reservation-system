package hotel.model;

import java.util.List;
import java.util.ArrayList;

public class Room {
    private String roomNumber;
    private String category;
    private double price;
    private boolean isAvailable;
    private List<String> amenities;

    public Room(String roomNumber, String category, double price, boolean isAvailable, List<String> amenities) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.amenities = amenities;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"roomNumber\":\"").append(roomNumber).append("\",");
        sb.append("\"category\":\"").append(category).append("\",");
        sb.append("\"price\":").append(price).append(",");
        sb.append("\"isAvailable\":").append(isAvailable).append(",");
        sb.append("\"amenities\":[");
        for (int i = 0; i < amenities.size(); i++) {
            sb.append("\"").append(amenities.get(i)).append("\"");
            if (i < amenities.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
}
