package com.github.cyrobdw.debughouse.ui;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;

public class Pockets extends GridPane {
    private final Board board;
    private final Side userSide;

    public Pockets(Board board, Side userSide) {
        this.board = board;
        this.userSide = userSide;
        render();
    }

    /**
     * Render pieces in hand
     */
    public void render() {
        getChildren().clear();
        String[] pieces;
        if (userSide.equals(Side.WHITE)) {
            pieces = new String[]{"p", "n", "b", "r", "q", "Q", "R", "B", "N", "P"};
        } else {
            pieces = new String[]{"P", "N", "B", "R", "Q", "q", "r", "b", "n", "p"};
        }
        HashMap<Piece, Integer> whitehand = board.gameState.getDisplayedHand(Side.WHITE);
        HashMap<Piece, Integer> blackhand = board.gameState.getDisplayedHand(Side.BLACK);
        for (int i = 0; i < pieces.length; i++) {
            Piece drop = Piece.fromFenSymbol(pieces[i]);
            int count;
            if (drop.getPieceSide().equals(Side.WHITE)) {
                count = whitehand.get(drop);
            } else {
                count = blackhand.get(drop);
            }
            Image image = new Image(getClass().getClassLoader().getResourceAsStream("images/" + drop.toString().toLowerCase() + ".png"));
            ImageView view = new ImageView();
            Rectangle square = new Rectangle();
            Rectangle selectBoundary = new Rectangle();
            StackPane pane = new StackPane();
            selectBoundary.setWidth(board.squareSize * 4 / 5);
            selectBoundary.setHeight(board.squareSize * 4 / 5);
            selectBoundary.setFill(Color.TRANSPARENT);
            view.setFitWidth(board.squareSize * 4 / 5);
            view.setFitHeight(board.squareSize * 4 / 5);
            if (count != 0) {
                view.setImage(image);
                Text text = new Text(50, 50, String.valueOf(count));
                Font font = Font.font("Serif", FontWeight.BOLD, 25 * board.scale);
                text.setFont(font);
                text.setFill(Color.WHITE);
                text.setTranslateX(30 * board.scale);
                text.setTranslateY(25 * board.scale);
                square.setWidth(30 * board.scale);
                square.setHeight(30 * board.scale);
                square.setArcWidth(10 * board.scale);
                square.setArcHeight(10 * board.scale);
                square.setFill(Color.web("#d04b15"));
                square.setTranslateX(30 * board.scale);
                square.setTranslateY(25 * board.scale);
                pane.getChildren().addAll(view, square, text, selectBoundary);
                if (i > 4) {
                    selectBoundary.setOnMousePressed((MouseEvent event) -> {
                        board.setSelectedDrop(drop);
                        event.consume();
                    });
                }
            } else {
                view.setOpacity(0);
                view.setImage(image);
                pane.getChildren().addAll(view, selectBoundary);
            }
            add(pane, 0, i);
        }
    }
}
