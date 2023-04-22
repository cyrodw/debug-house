package com.github.cyrobw.debughouse.ui;

import com.github.cyrobw.debughouse.SoundPlayer;
import com.github.cyrobw.debughouse.WebsocketClientEndpoint;
import com.github.bhlangonijr.chesslib.Side;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

public class Client extends Application {
    public static WebsocketClientEndpoint clientEndPoint;
    public static boolean connect = true;
    public static Board leftBoard;
    public static Board rightBoard;
    public static Chat chat;
    public static String username;
    public static String password;
    public static String ip;
    public static String host = "8080";

    @Override
    public void start(Stage stage) throws Exception {
        leftBoard = new Board(true);
        rightBoard = new Board(false);
        chat = new Chat();

        Stage stage1 = new Stage();
        stage1.initOwner(stage);

        Stage stage2 = new Stage();
        stage2.initOwner(stage);

        Stage stage3 = new Stage();
        stage3.initOwner(stage);

        leftBoard.start(stage1);
        rightBoard.start(stage2);
        chat.start(stage3);

        Preferences prefs = Preferences.userRoot().node("preferences");
        stage1.setOnCloseRequest(
                event -> {
                    prefs.putDouble("stage1_x", stage1.getX());
                    prefs.putDouble("stage1_y", stage1.getY());
                    prefs.putDouble("left_board_width", stage1.getWidth());
                    prefs.putDouble("left_board_height", stage1.getHeight());
                });

        stage2.setOnCloseRequest(
                event -> {
                    prefs.putDouble("stage2_x", stage2.getX());
                    prefs.putDouble("stage2_y", stage2.getY());
                    prefs.putDouble("right_board_width", stage2.getWidth());
                    prefs.putDouble("right_board_height", stage2.getHeight());
                });

        stage3.setOnCloseRequest(
                event -> {
                    prefs.putDouble("stage3_x", stage3.getX());
                    prefs.putDouble("stage3_y", stage3.getY());
                });

        double stage1_x = prefs.getDouble("stage1_x", -1);
        double stage1_y = prefs.getDouble("stage1_y", -1);
        if (stage1_x != -1) {
            stage1.setX(stage1_x);
            stage1.setY(stage1_y);
        }

        double stage2_x = prefs.getDouble("stage2_x", -1);
        double stage2_y = prefs.getDouble("stage2_y", -1);
        if (stage2_x != -1) {
            stage2.setX(stage2_x);
            stage2.setY(stage2_y);
        }

        double stage3_x = prefs.getDouble("stage3_x", -1);
        double stage3_y = prefs.getDouble("stage3_y", -1);
        if (stage3_x != -1) {
            stage3.setX(stage3_x);
            stage3.setY(stage3_y);
        }

        if (connect) {
            chat.receivedMessaged("Connecting to server...");
            connect(ip, host);
        }
    }

    /**
     * Relay message to chat.
     *
     * @param message
     */
    public static void sendToChat(String message) {
        chat.sendMessage(message);
    }

    /**
     * Send message to server.
     *
     * @param message
     */
    public static void sendToServer(String message) {
        if (connect) {
            clientEndPoint.sendMessage(message);
        }
    }

    /**
     * Connect to server and handle logic for callbacks.
     *
     * @param ip
     * @param host
     * @throws URISyntaxException
     */
    private void connect(String ip, String host) throws URISyntaxException {
        clientEndPoint = new WebsocketClientEndpoint(new URI("ws://" + ip + "/:" + host + "/?username=" + username + "&password=" + password));
        clientEndPoint.addMessageHandler(message -> {
            if (message.equals("connected")) {
                Platform.runLater(() -> {
                    chat.receivedMessaged("Connected!");
                });
            }
            String[] args = message.split(" ", 2);
            switch (args[0]) {
                case "message" -> Platform.runLater(() -> {
                    chat.receivedMessaged(args[1]);
                });
                case "started" -> Platform.runLater(() -> {
                    leftBoard.setPlaying(true);
                    rightBoard.setPlaying(true);
                    SoundPlayer.playSound("Gamestart.wav");
                });
                case "finished" -> Platform.runLater(() -> {
                    leftBoard.setPlaying(false);
                    rightBoard.setPlaying(false);
                    leftBoard.position.cancelPremoves();
                    leftBoard.position.render();
                    leftBoard.reset();
                    leftBoard.stopClocks();
                    rightBoard.reset();
                    rightBoard.stopClocks();
                    SoundPlayer.playSound("Checkmate.wav");
                });
                case "userside" -> Platform.runLater(() -> {
                    Side userSide = Side.fromValue(args[1].toUpperCase());
                    leftBoard.setUserSide(userSide);
                    rightBoard.setUserSide(userSide.flip());
                });
                case "fen1" -> Platform.runLater(() -> {
                    leftBoard.setPlaying(true);
                    leftBoard.setFen(args[1]);
                    leftBoard.render();
                });
                case "fen2" -> Platform.runLater(() -> {
                    rightBoard.setPlaying(true);
                    rightBoard.setFen(args[1]);
                    rightBoard.render();
                });
                case "move1" -> Platform.runLater(() -> {
                    if (args[1].length() > 0) {
                        leftBoard.pushMove(args[1]);
                    }
                });
                case "move2" -> Platform.runLater(() -> {
                    if (args[1].length() > 0) {
                        rightBoard.pushMove(args[1]);
                    }
                });
                case "whitehand1" -> Platform.runLater(() -> {
                    leftBoard.setHand(args[1].toUpperCase(), Side.WHITE);
                });
                case "blackhand1" -> Platform.runLater(() -> {
                    leftBoard.setHand(args[1].toLowerCase(), Side.BLACK);
                });
                case "whitehand2" -> Platform.runLater(() -> {
                    rightBoard.setHand(args[1].toUpperCase(), Side.WHITE);
                });
                case "blackhand2" -> Platform.runLater(() -> {
                    rightBoard.setHand(args[1].toLowerCase(), Side.BLACK);
                });
                case "times1" -> Platform.runLater(() -> {
                    leftBoard.setTimes(args[1].split(","), !leftBoard.userSide.equals(Side.WHITE));
                });
                case "times2" -> Platform.runLater(() -> {
                    rightBoard.setTimes(args[1].split(","), !rightBoard.userSide.equals(Side.WHITE));
                });
                case "players1" -> Platform.runLater(() -> {
                    String[] players = args[1].split(",");
                    if (leftBoard.userSide.equals(Side.WHITE)) {
                        leftBoard.username1 = players[1];
                        leftBoard.username2 = players[0];
                    } else {
                        leftBoard.username1 = players[0];
                        leftBoard.username2 = players[1];
                    }
                });
                case "players2" -> Platform.runLater(() -> {
                    String[] players = args[1].split(",");
                    if (rightBoard.userSide.equals(Side.WHITE)) {
                        rightBoard.username1 = players[1];
                        rightBoard.username2 = players[0];
                    } else {
                        rightBoard.username1 = players[0];
                        rightBoard.username2 = players[1];
                    }
                });
                case "ratings1" -> Platform.runLater(() -> {
                    String[] ratings = args[1].split(",");
                    if (leftBoard.userSide.equals(Side.WHITE)) {
                        leftBoard.rating1 = ratings[1];
                        leftBoard.rating2 = ratings[0];
                    } else {
                        leftBoard.rating1 = ratings[0];
                        leftBoard.rating2 = ratings[1];
                    }
                    leftBoard.createComponents();
                });
                case "ratings2" -> Platform.runLater(() -> {
                    String[] ratings = args[1].split(",");
                    if (rightBoard.userSide.equals(Side.WHITE)) {
                        rightBoard.rating1 = ratings[1];
                        rightBoard.rating2 = ratings[0];
                    } else {
                        rightBoard.rating1 = ratings[0];
                        rightBoard.rating2 = ratings[1];
                    }
                    rightBoard.createComponents();
                });
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
