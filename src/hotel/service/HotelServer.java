package hotel.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import hotel.model.Room;
import hotel.model.Reservation;
import hotel.model.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class HotelServer {
    private HttpServer server;
    private HotelSystem hotelSystem;

    public HotelServer(int port, HotelSystem hotelSystem) throws IOException {
        this.hotelSystem = hotelSystem;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/rooms", new RoomsHandler());
        server.createContext("/api/reservations", new ReservationsHandler());
        server.createContext("/api/book", new BookHandler());
        server.createContext("/api/cancel", new CancelHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());

        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("Hotel Reservation Web Server started at http://localhost:" + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void handleOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            File file = new File("hotel_web" + path);
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "File not found: " + path, "text/plain");
                return;
            }
            String contentType = "text/html";
            if (path.endsWith(".png")) {
                contentType = "image/png";
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (path.endsWith(".css")) {
                contentType = "text/css";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript";
            }
            
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }

    private class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            hotelSystem.load();
            String query = exchange.getRequestURI().getQuery();
            String category = "All";
            String checkIn = "";
            String checkOut = "";

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if (pair[0].equalsIgnoreCase("category")) {
                            category = pair[1];
                        } else if (pair[0].equalsIgnoreCase("checkIn")) {
                            checkIn = pair[1];
                        } else if (pair[0].equalsIgnoreCase("checkOut")) {
                            checkOut = pair[1];
                        }
                    }
                }
            }

            List<Room> availableRooms;
            if (checkIn.isEmpty() || checkOut.isEmpty()) {
                availableRooms = hotelSystem.getRooms();
            } else {
                availableRooms = hotelSystem.searchAvailableRooms(category, checkIn, checkOut);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < availableRooms.size(); i++) {
                sb.append(availableRooms.get(i).toJson());
                if (i < availableRooms.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");

            sendResponse(exchange, 200, sb.toString(), "application/json");
        }
    }

    private class ReservationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            hotelSystem.load();
            List<Reservation> list = hotelSystem.getReservations();
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).toJson());
                if (i < list.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");

            sendResponse(exchange, 200, sb.toString(), "application/json");
        }
    }

    private class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }

            String bodyStr = body.toString();
            String roomNumber = getJsonStringValue(bodyStr, "roomNumber");
            String guestName = getJsonStringValue(bodyStr, "guestName");
            String checkInDate = getJsonStringValue(bodyStr, "checkInDate");
            String checkOutDate = getJsonStringValue(bodyStr, "checkOutDate");

            if (roomNumber.isEmpty() || guestName.isEmpty() || checkInDate.isEmpty() || checkOutDate.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing required fields\"}", "application/json");
                return;
            }

            hotelSystem.load();
            Reservation res = hotelSystem.bookRoom(roomNumber, guestName, checkInDate, checkOutDate);
            if (res == null) {
                sendResponse(exchange, 400, "{\"error\":\"Room not available for selected dates\"}", "application/json");
                return;
            }

            sendResponse(exchange, 200, "{\"success\":true,\"reservation\":" + res.toJson() + "}", "application/json");
        }
    }

    private class CancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }

            String bodyStr = body.toString();
            String id = getJsonStringValue(bodyStr, "id");

            if (id.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing reservation ID\"}", "application/json");
                return;
            }

            hotelSystem.load();
            boolean success = hotelSystem.cancelReservation(id);
            if (!success) {
                sendResponse(exchange, 400, "{\"error\":\"Reservation not found\"}", "application/json");
                return;
            }

            sendResponse(exchange, 200, "{\"success\":true}", "application/json");
        }
    }

    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }

            String bodyStr = body.toString();
            String username = getJsonStringValue(bodyStr, "username");
            String password = getJsonStringValue(bodyStr, "password");
            String email = getJsonStringValue(bodyStr, "email");

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"All fields are required\"}", "application/json");
                return;
            }

            hotelSystem.load();
            String emailHtml = hotelSystem.registerUser(username, password, email);
            if (emailHtml == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Username already exists\"}", "application/json");
                return;
            }
            sendResponse(exchange, 200, "{\"success\":true}", "application/json");
        }
    }

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }

            String bodyStr = body.toString();
            String username = getJsonStringValue(bodyStr, "username");
            String password = getJsonStringValue(bodyStr, "password");

            if (username.isEmpty() || password.isEmpty()) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Username and password are required\"}", "application/json");
                return;
            }

            hotelSystem.load();
            User user = hotelSystem.authenticateUser(username, password);
            if (user == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Invalid username or password\"}", "application/json");
                return;
            }

            sendResponse(exchange, 200, "{\"success\":true,\"user\":" + user.toJson() + "}", "application/json");
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
        } else {
            int end = start;
            while (end < objStr.length() && objStr.charAt(end) != ',' && objStr.charAt(end) != '}' && objStr.charAt(end) != ']') {
                end++;
            }
            return objStr.substring(start, end).trim();
        }
        return "";
    }
}
