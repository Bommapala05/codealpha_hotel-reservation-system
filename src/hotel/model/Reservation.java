package hotel.model;

public class Reservation {
    private String id;
    private String roomNumber;
    private String guestName;
    private String checkInDate;
    private String checkOutDate;
    private double totalPrice;

    public Reservation(String id, String roomNumber, String guestName, String checkInDate, String checkOutDate, double totalPrice) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"roomNumber\":\"").append(roomNumber).append("\",");
        sb.append("\"guestName\":\"").append(guestName).append("\",");
        sb.append("\"checkInDate\":\"").append(checkInDate).append("\",");
        sb.append("\"checkOutDate\":\"").append(checkOutDate).append("\",");
        sb.append("\"totalPrice\":").append(totalPrice);
        sb.append("}");
        return sb.toString();
    }
}
