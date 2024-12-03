import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class UserHandler implements HttpHandler{
        private static final String DATABASE_URL = "jdbc:sqlite:users.db";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleCors(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String[] inputs = requestBody.split("\n", 2);
                String command = inputs[0].trim().toLowerCase();
                String data = inputs.length > 1 ? inputs[1].trim() : "";

                if ("login".equals(command) || "signup".equals(command) || "echo".equals(command)) {
                    String response = switch (command) {
                        case "signup" -> handleSignup(data);
                        case "login" -> handleLogin(data);
                        case "echo" -> handleEcho(data);
                        default -> "Invalid command. Use 'signup' or 'login'.";
                    };
                    sendResponse(exchange, 200, response);
                    return;
                }

                String token = exchange.getRequestHeaders().getFirst("Authorization");
                if (token == null || !token.startsWith("Bearer ")) {
                    sendResponse(exchange, 401, "Unauthorized: Token required");
                    return;
                }

                String authToken = token.substring(7);
                if (!isValidToken(authToken)) {
                    sendResponse(exchange, 401, "Unauthorized: Invalid token");
                    return;
                }

                sendResponse(exchange, 200, "Command received.");
            } else {
                sendResponse(exchange, 405, "Invalid request method. Use POST.");
            }
        }

    private String handleEcho(String data) {
        return data; // Simply echo back the input data
    }


    private void handleCors(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.sendResponseHeaders(204, -1);
        }

        private boolean isValidToken(String token) {
            return token.equals("valid-auth-token");
        }

        private String handleSignup(String data) {
            String[] parts = data.split("\n");
            if (parts.length != 2) return "Invalid signup format. Provide 'name\\npassword'.";

            String name = parts[0].trim();
            String password = parts[1].trim();

            String insertUserSQL = "INSERT INTO users (name, password) VALUES (?, ?)";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(insertUserSQL)) {
                pstmt.setString(1, name);
                pstmt.setString(2, password);
                pstmt.executeUpdate();
                return "Signup successful for user: " + name;
            } catch (SQLException e) {
                return "Signup failed: " + e.getMessage();
            }
        }

        private String handleLogin(String data) {
            String[] parts = data.split("\n");
            if (parts.length != 2) return "Invalid login format. Provide 'name\\npassword'.";

            String name = parts[0].trim();
            String password = parts[1].trim();

            String selectUserSQL = "SELECT * FROM users WHERE name = ? AND password = ?";
            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(selectUserSQL)) {
                pstmt.setString(1, name);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return "Login successful. Welcome, " + name + "!";
                    } else {
                        return "Invalid credentials.";
                    }
                }
            } catch (SQLException e) {
                return "Login failed: " + e.getMessage();
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(statusCode, responseText.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseText.getBytes());
            }
        }
    }


