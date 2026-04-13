import java.io.*;
import java.net.*;

class ClientHandler extends Thread {

    Socket socket;
    BufferedReader in;
    PrintWriter out;

    String username;
    String roomId;

    public ClientHandler(Socket socket) throws Exception {
        this.socket = socket;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void run() {

        try {
            out.println("Enter username:");
            username = in.readLine();

            out.println("Enter room ID:");
            roomId = in.readLine();

            Server.users.put(username, this);

            GameRoom room = Server.getRoom(roomId);
            room.addPlayer(username);

            room.broadcast(username + " joined Room " + roomId);

            String msg;

            while ((msg = in.readLine()) != null) {

                // GAME MASTER SET QUESTION
                if (msg.startsWith("Q:")) {
                    room.question = msg.substring(2);
                    out.println("Enter answer:");
                    room.answer = in.readLine();
                    room.gameMaster = username;
                    room.broadcast("Question set in Room " + roomId);
                }

                // START GAME
                else if (msg.equalsIgnoreCase("START")) {
                    room.startGame();
                }

                // GUESS
                else {
                    room.guess(username, msg);
                }
            }

        } catch (Exception e) {
            System.out.println("Client disconnected");
        }
    }

    void send(String msg) {
        out.println(msg);
    }
}