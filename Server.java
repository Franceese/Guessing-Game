import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    static ServerSocket serverSocket;

    // ROOM MANAGEMENT
    static Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    // CLIENT TRACKING
    static Map<String, ClientHandler> users = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(System.getenv("PORT"));
ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server running with rooms...");

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }

    // ---------------- ROOM GET OR CREATE ----------------
    static GameRoom getRoom(String roomId) {
        return rooms.computeIfAbsent(roomId, GameRoom::new);
    }

    // ---------------- BROADCAST ROOM ----------------
    static void broadcastToRoom(String roomId, String msg) {

        for (ClientHandler c : users.values()) {
            if (roomId.equals(c.roomId)) {
                c.send(msg);
            }
        }
    }

    // ---------------- SEND TO USER ----------------
    static void sendToUser(String user, String msg) {
        ClientHandler c = users.get(user);
        if (c != null) c.send(msg);
    }

    // ---------------- REMOVE ROOM ----------------
    static void endRoom(String roomId) {
        GameRoom room = rooms.get(roomId);

        if (room != null && room.players.isEmpty()) {
            rooms.remove(roomId);
        }
    }
}