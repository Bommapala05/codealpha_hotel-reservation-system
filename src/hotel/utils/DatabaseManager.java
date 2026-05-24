package hotel.utils;

import hotel.model.Room;
import hotel.model.Reservation;
import hotel.model.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    public static void loadData(String filePath, List<Room> rooms, List<Reservation> reservations, List<User> users) {
        rooms.clear();
        reservations.clear();
        users.clear();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            content = content.trim();

            int roomsStart = content.indexOf("\"rooms\":");
            if (roomsStart != -1) {
                int startBracket = content.indexOf("[", roomsStart);
                int endBracket = findClosingBracket(content, startBracket);
                if (startBracket != -1 && endBracket != -1) {
                    String roomsContent = content.substring(startBracket + 1, endBracket);
                    parseRooms(roomsContent, rooms);
                }
            }

            int resStart = content.indexOf("\"reservations\":");
            if (resStart != -1) {
                int startBracket = content.indexOf("[", resStart);
                int endBracket = findClosingBracket(content, startBracket);
                if (startBracket != -1 && endBracket != -1) {
                    String resContent = content.substring(startBracket + 1, endBracket);
                    parseReservations(resContent, reservations);
                }
            }

            int usersStart = content.indexOf("\"users\":");
            if (usersStart != -1) {
                int startBracket = content.indexOf("[", usersStart);
                int endBracket = findClosingBracket(content, startBracket);
                if (startBracket != -1 && endBracket != -1) {
                    String usersContent = content.substring(startBracket + 1, endBracket);
                    parseUsers(usersContent, users);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int findClosingBracket(String str, int startIdx) {
        int count = 0;
        for (int i = startIdx; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '[') {
                count++;
            } else if (ch == ']') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static void parseRooms(String content, List<Room> rooms) {
        int idx = 0;
        while ((idx = content.indexOf("{", idx)) != -1) {
            int endIdx = content.indexOf("}", idx);
            if (endIdx == -1) break;
            String roomStr = content.substring(idx + 1, endIdx);
            
            String roomNumber = getJsonStringValue(roomStr, "roomNumber");
            String category = getJsonStringValue(roomStr, "category");
            double price = getJsonDoubleValue(roomStr, "price");
            boolean isAvailable = getJsonBooleanValue(roomStr, "isAvailable");
            List<String> amenities = getJsonArrayValue(roomStr, "amenities");

            rooms.add(new Room(roomNumber, category, price, isAvailable, amenities));
            idx = endIdx + 1;
        }
    }

    private static void parseReservations(String content, List<Reservation> reservations) {
        int idx = 0;
        while ((idx = content.indexOf("{", idx)) != -1) {
            int endIdx = content.indexOf("}", idx);
            if (endIdx == -1) break;
            String resStr = content.substring(idx + 1, endIdx);

            String id = getJsonStringValue(resStr, "id");
            String roomNumber = getJsonStringValue(resStr, "roomNumber");
            String guestName = getJsonStringValue(resStr, "guestName");
            String checkInDate = getJsonStringValue(resStr, "checkInDate");
            String checkOutDate = getJsonStringValue(resStr, "checkOutDate");
            double totalPrice = getJsonDoubleValue(resStr, "totalPrice");

            reservations.add(new Reservation(id, roomNumber, guestName, checkInDate, checkOutDate, totalPrice));
            idx = endIdx + 1;
        }
    }

    private static void parseUsers(String content, List<User> users) {
        int idx = 0;
        while ((idx = content.indexOf("{", idx)) != -1) {
            int endIdx = content.indexOf("}", idx);
            if (endIdx == -1) break;
            String userStr = content.substring(idx + 1, endIdx);

            String username = getJsonStringValue(userStr, "username");
            String password = getJsonStringValue(userStr, "password");
            String email = getJsonStringValue(userStr, "email");

            users.add(new User(username, password, email));
            idx = endIdx + 1;
        }
    }

    private static String getJsonStringValue(String objStr, String key) {
        String target = "\"" + key + "\":";
        int start = objStr.indexOf(target);
        if (start == -1) return "";
        start += target.length();
        while (start < objStr.length() && (objStr.charAt(start) == ' ' || objStr.charAt(start) == '\t')) {
            start++;
        }
        if (objStr.charAt(start) == '"') {
            int end = objStr.indexOf("\"", start + 1);
            if (end != -1) {
                return objStr.substring(start + 1, end);
            }
        }
        return "";
    }

    private static double getJsonDoubleValue(String objStr, String key) {
        String target = "\"" + key + "\":";
        int start = objStr.indexOf(target);
        if (start == -1) return 0.0;
        start += target.length();
        int end = start;
        while (end < objStr.length() && objStr.charAt(end) != ',' && objStr.charAt(end) != '}' && objStr.charAt(end) != '\n' && objStr.charAt(end) != '\r') {
            end++;
        }
        try {
            return Double.parseDouble(objStr.substring(start, end).trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static boolean getJsonBooleanValue(String objStr, String key) {
        String target = "\"" + key + "\":";
        int start = objStr.indexOf(target);
        if (start == -1) return false;
        start += target.length();
        int end = start;
        while (end < objStr.length() && objStr.charAt(end) != ',' && objStr.charAt(end) != '}' && objStr.charAt(end) != '\n' && objStr.charAt(end) != '\r') {
            end++;
        }
        return Boolean.parseBoolean(objStr.substring(start, end).trim());
    }

    private static List<String> getJsonArrayValue(String objStr, String key) {
        List<String> list = new ArrayList<>();
        String target = "\"" + key + "\":";
        int start = objStr.indexOf(target);
        if (start == -1) return list;
        int startBracket = objStr.indexOf("[", start);
        int endBracket = objStr.indexOf("]", startBracket);
        if (startBracket == -1 || endBracket == -1) return list;
        String arrStr = objStr.substring(startBracket + 1, endBracket);
        String[] items = arrStr.split(",");
        for (String item : items) {
            item = item.trim();
            if (item.startsWith("\"") && item.endsWith("\"")) {
                list.add(item.substring(1, item.length() - 1));
            }
        }
        return list;
    }

    public static void saveData(String filePath, List<Room> rooms, List<Reservation> reservations, List<User> users) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"rooms\": [\n");
            for (int i = 0; i < rooms.size(); i++) {
                sb.append("    ").append(rooms.get(i).toJson());
                if (i < rooms.size() - 1) {
                    sb.append(",\n");
                } else {
                    sb.append("\n");
                }
            }
            sb.append("  ],\n");
            sb.append("  \"reservations\": [\n");
            for (int i = 0; i < reservations.size(); i++) {
                sb.append("    ").append(reservations.get(i).toJson());
                if (i < reservations.size() - 1) {
                    sb.append(",\n");
                } else {
                    sb.append("\n");
                }
            }
            sb.append("  ],\n");
            sb.append("  \"users\": [\n");
            for (int i = 0; i < users.size(); i++) {
                sb.append("    ").append(users.get(i).toJson());
                if (i < users.size() - 1) {
                    sb.append(",\n");
                } else {
                    sb.append("\n");
                }
            }
            sb.append("  ]\n");
            sb.append("}");

            Files.write(Paths.get(filePath), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
