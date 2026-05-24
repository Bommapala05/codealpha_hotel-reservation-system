package hotel.utils;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class EmailService {

    private static String smtpHost = "";
    private static int smtpPort = 465;
    private static String username = "";
    private static String password = "";
    private static String fromEmail = "";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            File configFile = new File("email_config.json");
            if (!configFile.exists()) {
                return;
            }
            String content = new String(Files.readAllBytes(Paths.get("email_config.json")), StandardCharsets.UTF_8);
            smtpHost = getJsonStringValue(content, "smtpHost");
            fromEmail = getJsonStringValue(content, "fromEmail");
            username = getJsonStringValue(content, "username");
            password = getJsonStringValue(content, "password");
            
            String portStr = getJsonStringValue(content, "smtpPort");
            if (!portStr.isEmpty()) {
                smtpPort = Integer.parseInt(portStr);
            }
        } catch (Exception e) {
            smtpHost = "";
        }
    }

    private static String getJsonStringValue(String objStr, String key) {
        String target = "\"" + key + "\":";
        int start = objStr.indexOf(target);
        if (start == -1) return "";
        start += target.length();
        while (start < objStr.length() && (objStr.charAt(start) == ' ' || objStr.charAt(start) == '\t' || objStr.charAt(start) == '\n' || objStr.charAt(start) == '\r')) {
            start++;
        }
        if (objStr.charAt(start) == '"') {
            int end = objStr.indexOf("\"", start + 1);
            if (end != -1) {
                return objStr.substring(start + 1, end);
            }
        } else {
            int end = start;
            while (end < objStr.length() && objStr.charAt(end) != ',' && objStr.charAt(end) != '}' && objStr.charAt(end) != '\n' && objStr.charAt(end) != '\r') {
                end++;
            }
            return objStr.substring(start, end).trim();
        }
        return "";
    }

    public static String sendRegistrationEmail(String toEmail, String registeredUsername) {
        loadConfig();
        
        String subject = "Welcome to The Cove Sanctuary!";
        String body = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f7f5; margin: 0; padding: 40px; color: #132a13;'>" +
                "<div style='max-width: 600px; background-color: #ffffff; border: 1px solid rgba(45, 106, 79, 0.15); border-radius: 12px; padding: 40px; margin: 0 auto; box-shadow: 0 4px 12px rgba(0,0,0,0.03);'>" +
                "<div style='text-align: center; border-bottom: 1px solid rgba(45, 106, 79, 0.12); padding-bottom: 20px; margin-bottom: 30px;'>" +
                "<h1 style='font-family: Georgia, serif; color: #1b4332; font-size: 28px; margin: 0; text-transform: uppercase; letter-spacing: 2px;'>THE COVE</h1>" +
                "<p style='font-size: 12px; color: #526e5d; margin: 5px 0 0 0; text-transform: uppercase; letter-spacing: 1.5px;'>Luxury Nature Sanctuary</p>" +
                "</div>" +
                "<h2>Registration Confirmed</h2>" +
                "<p>Dear <strong>" + registeredUsername + "</strong>,</p>" +
                "<p>Welcome to The Cove. We are delighted to confirm your profile registration at our luxury nature sanctuary web portal.</p>" +
                "<p>You can now browse our standard, deluxe, and presidential suites, verify live availability ranges, and reserve your luxury stay with our simulated checkouts.</p>" +
                "<div style='background-color: rgba(82, 183, 136, 0.08); border-left: 4px solid #2d6a4f; padding: 15px 20px; border-radius: 4px; margin: 25px 0;'>" +
                "<p style='margin: 0; font-size: 14px;'><strong>Account Username:</strong> " + registeredUsername + "</p>" +
                "<p style='margin: 5px 0 0 0; font-size: 14px;'><strong>Linked Email:</strong> " + toEmail + "</p>" +
                "</div>" +
                "<p>If you have any requests or need bespoke concierge arrangements during your simulated stay, please contact our team.</p>" +
                "<p style='margin-top: 40px; font-size: 14px; color: #526e5d;'>Warmest regards,<br><strong>The Cove Concierge Team</strong></p>" +
                "</div>" +
                "</body>" +
                "</html>";

        if (username.isEmpty() || password.isEmpty() || fromEmail.isEmpty() || smtpHost.isEmpty()) {
            writeToLog(toEmail, subject, body);
            return body;
        }

        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(smtpHost, smtpPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            reader.readLine();

            writer.write("EHLO " + smtpHost + "\r\n");
            writer.flush();
            readMultiLine(reader);

            writer.write("AUTH LOGIN\r\n");
            writer.flush();
            reader.readLine();

            writer.write(Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)) + "\r\n");
            writer.flush();
            reader.readLine();

            writer.write(Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)) + "\r\n");
            writer.flush();
            reader.readLine();

            writer.write("MAIL FROM:<" + fromEmail + ">\r\n");
            writer.flush();
            reader.readLine();

            writer.write("RCPT TO:<" + toEmail + ">\r\n");
            writer.flush();
            reader.readLine();

            writer.write("DATA\r\n");
            writer.flush();
            reader.readLine();

            writer.write("Subject: " + subject + "\r\n");
            writer.write("From: The Cove Sanctuary <" + fromEmail + ">\r\n");
            writer.write("To: " + toEmail + "\r\n");
            writer.write("MIME-Version: 1.0\r\n");
            writer.write("Content-Type: text/html; charset=utf-8\r\n");
            writer.write("\r\n");
            writer.write(body);
            writer.write("\r\n.\r\n");
            writer.flush();
            reader.readLine();

            writer.write("QUIT\r\n");
            writer.flush();
            socket.close();

            System.out.println("Registration confirmation email successfully sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("SMTP Error sending email: " + e.getMessage());
            writeToLog(toEmail, subject, body);
        }
        return body;
    }

    private static void readMultiLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.charAt(3) == ' ') {
                break;
            }
        }
    }

    private static void writeToLog(String toEmail, String subject, String body) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================================\n");
            sb.append("EMAIL SIMULATION LOG\n");
            sb.append("Recipient: ").append(toEmail).append("\n");
            sb.append("Subject: ").append(subject).append("\n");
            sb.append("Content (HTML):\n").append(body).append("\n");
            sb.append("========================================================================\n\n");

            Files.write(Paths.get("email_sent_log.txt"), sb.toString().getBytes(StandardCharsets.UTF_8), 
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            
            System.out.println("Email configuration missing/incorrect. Simulated email saved in email_sent_log.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
