import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class RoomHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Read the incoming message
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Received: " + requestBody); // Debug: print incoming request

            // Echo the message back
            String response = requestBody; // Send back only the message
            System.out.println("Response Sent: " + response); // Debug: print the outgoing response

            // Send the response
            sendResponse(exchange, 200, response);
        } else {
            sendResponse(exchange, 405, "Invalid request method. Use POST.");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, responseText.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseText.getBytes(StandardCharsets.UTF_8));
        }
    }

}
