package com.github.cyrodw.debughouse.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Clock extends Pane {
    private int deciseconds;
    private boolean running;
    private Timeline timeline;
    private Text clockText;
    private final Rectangle background;
    private final double HEIGHT;
    private final double WIDTH;
    private final Board board;

    private static final int lowWarningThreshold = 100;

    public Clock(Board board, int deciseconds) {
        this.board = board;
        this.deciseconds = deciseconds;
        this.running = false;

        HEIGHT = board.squareSize * 4 / 5;
        WIDTH = board.squareSize * 2;

        background = new Rectangle();

        background.setWidth(WIDTH);
        background.setHeight(HEIGHT);

        background.setArcWidth(10 * board.scale);
        background.setArcHeight(10 * board.scale);

        background.setFill(Color.color(0.2f, 0.2f, 0.2f, 1.0f));

        this.getChildren().add(background);
    }

    /**
     * Returns the formatted time from the deciseconds.
     */
    public String getFormattedTime() {
        int seconds = deciseconds / 10;
        int minutes = seconds / 60;
        return minutes + ":" + String.format("%02d", seconds - minutes * 60) + "." + deciseconds % 10;
    }

    /**
     * Sets the time of the clock and renders
     *
     * @param deciseconds
     */
    public void setTime(int deciseconds) {
        this.deciseconds = deciseconds;
        render();
    }

    /**
     * Start clock animation
     */
    public void start() {
        if (deciseconds < 100) {
            background.setFill(Color.web("#7b2c2b"));
        } else {
            background.setFill(Color.web("#39561FFF"));
        }
        running = true;
        if (timeline == null) {
            timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(100),
                            actionEvent -> {
                                if (running) {
                                    deciseconds -= 1;
                                    if (deciseconds <= 0) {
                                        deciseconds = 0;
                                        stop();
                                    }
                                    if (deciseconds < lowWarningThreshold) {
                                        background.setFill(Color.web("#7b2c2b"));
                                    }
                                    render();
                                }
                            })
            );
            timeline.playFromStart();
        }
    }

    /**
     * Stop clock
     */
    public void stop() {
        if (deciseconds < lowWarningThreshold) {
            background.setFill(Color.web("#512827"));
        } else {
            background.setFill(Color.color(0.2f, 0.2f, 0.2f, 1.0f));
        }
        running = false;
        render();
    }

    /**
     * Render clock component.
     */
    public void render() {
        this.getChildren().remove(clockText);
        clockText = new Text(WIDTH, HEIGHT, getFormattedTime());
        Font font = Font.font("", FontWeight.BOLD, 45 * board.scale);
        clockText.setFont(font);
        if (running) {
            clockText.setFill(Color.WHITE);
        } else {
            clockText.setFill(Color.color(0.5f, 0.5f, 0.5f, 1.0f));
        }
        clockText.setX(background.getWidth() / 2 - clockText.getLayoutBounds().getWidth() / 2);
        clockText.setY(background.getHeight() / 2 + 15 * board.scale);
        this.getChildren().add(clockText);
    }
}
