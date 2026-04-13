import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientFX extends Application {

    TextArea chatArea = new TextArea();
    TextField input = new TextField();
    Button sendBtn = new Button("Send");

    Label timerLabel = new Label("⏱ Timer: --");
    Label statusLabel = new Label("Status: Waiting...");

    ListView<String> playerList = new ListView<>();

    PrintWriter out;

    @Override
    public void start(Stage stage) throws Exception {

        Socket socket = new Socket("localhost", 5000);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        out = new PrintWriter(socket.getOutputStream(), true);

        // ---------------- UI SETUP ----------------
        chatArea.setEditable(false);
        chatArea.setPrefHeight(300);

        input.setPrefWidth(250);

        sendBtn.setOnAction(e -> sendMessage());

        HBox inputBox = new HBox(input, sendBtn);
        inputBox.setSpacing(10);

        VBox left = new VBox(chatArea, inputBox);
        left.setSpacing(10);

        VBox right = new VBox(
                new Label("Players"),
                playerList,
                timerLabel,
                statusLabel
        );

        right.setSpacing(10);

        HBox root = new HBox(left, right);
        root.setSpacing(20);

        Scene scene = new Scene(root, 750, 400);

        stage.setTitle("Guessing Game");
        stage.setScene(scene);
        stage.show();

        // ---------------- LISTEN SERVER ----------------
        new Thread(() -> {

            String msg;

            try {
                while ((msg = in.readLine()) != null) {
                    handleMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    // ---------------- HANDLE MESSAGES ----------------
    void handleMessage(String msg) {

        Platform.runLater(() -> {

            // TIMER
            if (msg.startsWith("⏱ Time left")) {
                timerLabel.setText(msg);
                return;
            }

            // GAME END
            if (msg.contains("TIME UP") || msg.contains("WON")) {
                statusLabel.setText("Game Ended");
                input.setDisable(true);
            }

            // SCORES
            if (msg.contains("pts")) {
                chatArea.appendText(msg + "\n");
                return;
            }

            // PLAYER JOIN TRACKING
            if (msg.contains("joined")) {
                String player = msg.split(" ")[0];

                if (!playerList.getItems().contains(player)) {
                    playerList.getItems().add(player);
                }
            }

            chatArea.appendText(msg + "\n");
        });
    }

    // ---------------- SEND MESSAGE ----------------
    void sendMessage() {
        String msg = input.getText();

        if (msg.isEmpty()) return;

        out.println(msg);
        input.clear();
    }

    public static void main(String[] args) {
        launch();
    }
}