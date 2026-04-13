import java.util.*;
import java.util.concurrent.*;

public class GameRoom {

    String roomId;

    String gameMaster;
    String question;
    String answer;

    boolean gameActive = false;

    List<String> players = new CopyOnWriteArrayList<>();
    Map<String, Integer> scores = new ConcurrentHashMap<>();
    Map<String, Integer> attempts = new ConcurrentHashMap<>();

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    int timeLeft = 60;

    public GameRoom(String roomId) {
        this.roomId = roomId;
    }

    // ---------------- ADD PLAYER ----------------
    public void addPlayer(String username) {
        if (!players.contains(username)) {
            players.add(username);
            scores.putIfAbsent(username, 0);
            attempts.put(username, 3);
        }
    }

    // ---------------- BROADCAST HELPER ----------------
    public void broadcast(String msg) {
        Server.broadcastToRoom(roomId, msg);
    }

    // ---------------- START GAME ----------------
    public void startGame() {

        if (players.size() < 3) {
            broadcast("Need at least 3 players");
            return;
        }

        gameActive = true;
        timeLeft = 60;

        broadcast("🚀 Game Started in Room " + roomId);
        broadcast("Question: " + question);

        startTimer();
    }

    // ---------------- TIMER ----------------
    public void startTimer() {

        scheduler.scheduleAtFixedRate(() -> {

            if (!gameActive) return;

            broadcast("⏱ [" + roomId + "] Time left: " + timeLeft + "s");
            timeLeft--;

            if (timeLeft < 0) {
                gameActive = false;

                broadcast("⏱ TIME UP in Room " + roomId);
                broadcast("Answer: " + answer);

                broadcastScores();
                Server.endRoom(roomId);
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    // ---------------- GUESS ----------------
    public void guess(String user, String guess) {

        if (!gameActive) return;

        int att = attempts.getOrDefault(user, 0);

        if (att <= 0) {
            Server.sendToUser(user, "No attempts left");
            return;
        }

        attempts.put(user, att - 1);

        if (guess.equalsIgnoreCase(answer)) {

            gameActive = false;

            broadcast("🏆 " + user + " WON in Room " + roomId);
            broadcast("Answer: " + answer);

            scores.put(user, scores.get(user) + 10);

            broadcastScores();
            Server.endRoom(roomId);

        } else {
            Server.sendToUser(user, "Wrong! Attempts left: " + (att - 1));
        }
    }

    // ---------------- SCORES ----------------
    public void broadcastScores() {
        broadcast("---- SCORES (Room " + roomId + ") ----");

        for (String p : scores.keySet()) {
            broadcast(p + " : " + scores.get(p));
        }
    }
}