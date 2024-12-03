import java.io.*;
import java.net.*;
import java.sql.*;
import com.sun.net.httpserver.HttpServer;
public class ChatServer {
    private static final String DATABASE_URL = "jdbc:sqlite:users.db";
    public static void main(String[] args) throws IOException {
        initializeDatabase();
        // Start the HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8189), 0);
        server.createContext("/user", new UserHandler());
        server.createContext("/room", new RoomHandler());
        server.setExecutor(null); // creates a default executor
        System.out.println("Server is running on port 8189...");
        server.start();
    }

    private static void initializeDatabase() {
        String createUsersTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    name TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL
                );
                """;

//        String createRoomsTableSQL = """
//                CREATE TABLE IF NOT EXISTS rooms (
//                    room_name TEXT NOT NULL UNIQUE
//                );
//                """;
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTableSQL);
            //stmt.execute(createRoomsTableSQL);
            System.out.println("Database initialized with 'users' and 'rooms' tables ready.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
}

