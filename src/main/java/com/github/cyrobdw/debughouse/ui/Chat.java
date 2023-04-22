package com.github.cyrobdw.debughouse.ui;

import com.github.cyrobdw.debughouse.SoundPlayer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.prefs.Preferences;

public class Chat extends Application {
    private TextField messageInput;   // For entering messages to be sent to the chat room.
    private TextArea transcript;
    private final ArrayList<String> commandHistory = new ArrayList<>();
    private int index;
    private long lastSendTime = 0;
    public static double HEIGHT = 196;
    public static double WIDTH = 392;

    @Override
    public void start(Stage stage) {
        transcript = new TextArea();
        transcript.setFont(Font.font("", 15));
        transcript.setPrefRowCount(60);
        transcript.setPrefColumnCount(500);
        transcript.setWrapText(true);
        transcript.setEditable(false);
        transcript.requestFocus();
        transcript.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/stylesheets/chat.css")).toExternalForm());

        // Sends the contents of the messageInput.
        Button sendButton = new Button("Enter");
        sendButton.setOnAction(event -> {
            String message = messageInput.getText();
            sendMessage(message);
            messageInput.clear();
        });
        messageInput = new TextField();
        messageInput.setPrefColumnCount(500);
        messageInput.setPromptText("Type /help for commands...");

        HBox bottom = new HBox(8, messageInput);
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        messageInput.setFont(Font.font("", 15));
        messageInput.setPrefHeight(35);
        BorderPane root = new BorderPane(transcript);
        root.setBottom(bottom);

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        messageInput.setOnKeyPressed(event -> {
            if (!commandHistory.isEmpty()) {
                if (event.getCode() == KeyCode.UP) {
                    index = Math.max(index - 1, 0);
                    messageInput.setText(commandHistory.get(index));
                }
                if (event.getCode() == KeyCode.DOWN) {
                    index = Math.min(index + 1, commandHistory.size() - 1);
                    messageInput.setText(commandHistory.get(index));
                }
            }
        });
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = messageInput.getText();
                sendMessage(message);
                messageInput.clear();
            }
        });

        stage.setTitle("Chat");
        stage.setScene(scene);

        Preferences prefs = Preferences.userRoot().node("preferences");
        stage.setHeight(prefs.getDouble("chat_height", HEIGHT));
        stage.setWidth(prefs.getDouble("chat_width", WIDTH));

        stage.setOnCloseRequest(
                event -> {
                    prefs.putDouble("chat_height", stage.getHeight());
                    prefs.putDouble("chat_width", stage.getWidth());
                });
        stage.show();
        messageInput.requestFocus();
    }


    public void sendMessage(String message) {
        if (message.isEmpty()) {
            return;
        }

        lastSendTime = System.currentTimeMillis();
        if (message.equals("/help")) {
            transcript.appendText(message + "\n");
            transcript.appendText("Commands: \n /seek x - Seek a game with a time control of \"x\" minutes \n /partner x - Send partnership request to \"x\" \n /resign - Resigns game \n /rematch x - Rematch current opponent \n");
        } else if (message.startsWith("/")) {
            Client.sendToServer(message.substring(1));
            transcript.appendText(message + "\n");
            if (commandHistory.isEmpty() || !commandHistory.get(commandHistory.size() - 1).equals(message)) {
                commandHistory.add(message);
            }
            index = commandHistory.size();
        } else {
            Client.sendToServer("message " + message);
        }
    }

    public void receivedMessaged(String message) {
        if (message.equals("pong")) {
            transcript.appendText((System.currentTimeMillis() - lastSendTime) + "ms\n");
            return;
        }
        if (message.contains(":bughouse-bp")) {
            SoundPlayer.playSound("Pgood.wav");
        }
        if (message.contains(":bughouse-bn")) {
            SoundPlayer.playSound("Ngood.wav");
        }
        if (message.contains(":bughouse-bb")) {
            SoundPlayer.playSound("Bgood.wav");
        }
        if (message.contains(":bughouse-br")) {
            SoundPlayer.playSound("Rgood.wav");
        }
        if (message.contains(":bughouse-bq")) {
            SoundPlayer.playSound("Qgood.wav");
        }
        if (message.contains(":bughouse-no-bp")) {
            SoundPlayer.playSound("NoP.wav");
        }
        if (message.contains(":bughouse-no-bn")) {
            SoundPlayer.playSound("Non.wav");
        }
        if (message.contains(":bughouse-no-bb")) {
            SoundPlayer.playSound("Nob.wav");
        }
        if (message.contains(":bughouse-no-br")) {
            SoundPlayer.playSound("Nor.wav");
        }
        if (message.contains(":bughouse-no-bq")) {
            SoundPlayer.playSound("Qbad.wav");
        }
        if (message.contains(":bughouse-st")) {
            SoundPlayer.playSound("Sit.wav");
        }
        if (message.contains(":bughouse-ns")) {
            SoundPlayer.playSound("Go.wav");
        }
        if (message.contains(":bughouse-tp")) {
            SoundPlayer.playSound("Tradesgood.wav");
        }
        if (message.contains(":bughouse-nt")) {
            SoundPlayer.playSound("Tradesbad.wav");
        }
        if (message.contains(":bughouse-mo")) {
            SoundPlayer.playSound("Oppmated.wav");
        }
        if (message.contains(":bughouse")) {
            message = message.replace(":bughouse-bp", "Pawn good.");
            message = message.replace(":bughouse-bn", "Knight good.");
            message = message.replace(":bughouse-bb", "Bishop good.");
            message = message.replace(":bughouse-br", "Rook good.");
            message = message.replace(":bughouse-bq", "Queen good.");
            message = message.replace(":bughouse-no-bp", "Pawn bad!");
            message = message.replace(":bughouse-no-bn", "Knight bad!");
            message = message.replace(":bughouse-no-bb", "Bishop bad!");
            message = message.replace(":bughouse-no-br", "Rook bad!");
            message = message.replace(":bughouse-no-bq", "Queen bad!");
            message = message.replace(":bughouse-tp", "Trades good.");
            message = message.replace(":bughouse-nt", "Trades bad!");
            message = message.replace(":bughouse-st", "Sit!");
            message = message.replace(":bughouse-mo", "I have mate.");
            message = message.replace(":bughouse-ns", "Go!");
        }
        transcript.appendText(message + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
