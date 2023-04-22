package com.github.cyrobw.debughouse.ui;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;

import java.util.HashMap;
import java.util.Objects;

public class BoardField extends Label {

    private Image image = null;
    private final int x; // The x position of the field on the board
    private final int y; // The y position
    private int toX; // Destination x used when calculating the move made
    private int toY;
    private double dragDeltaX, dragDeltaY;
    private Board board;

    public static HashMap<Piece, Image> pieceToImage = new HashMap<>();

    static {
        pieceToImage.put(Piece.WHITE_PAWN, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/white_pawn.png"))));
        pieceToImage.put(Piece.WHITE_KNIGHT, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/white_knight.png"))));
        pieceToImage.put(Piece.WHITE_BISHOP, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/white_bishop.png"))));
        pieceToImage.put(Piece.WHITE_ROOK, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/white_rook.png"))));
        pieceToImage.put(Piece.WHITE_QUEEN, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/white_queen.png"))));
        pieceToImage.put(Piece.WHITE_KING, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/white_king.png"))));
        pieceToImage.put(Piece.BLACK_PAWN, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/black_pawn.png"))));
        pieceToImage.put(Piece.BLACK_KNIGHT, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/black_knight.png"))));
        pieceToImage.put(Piece.BLACK_BISHOP, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/black_bishop.png"))));
        pieceToImage.put(Piece.BLACK_ROOK, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/black_rook.png"))));
        pieceToImage.put(Piece.BLACK_QUEEN, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/black_queen.png"))));
        pieceToImage.put(Piece.BLACK_KING, new Image(Objects.requireNonNull(BoardField.class.getClassLoader().getResourceAsStream("images/black_king.png"))));
    }

    public BoardField(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;

        setMinSize(board.squareSize, board.squareSize);
        setMaxSize(board.squareSize, board.squareSize);

        setOnMousePressed(this::onMousePressed);
        setOnMouseDragged(this::onMouseDragged);
        setOnMouseReleased(this::onMouseReleased);
    }

    /**
     * Callback when mouse is pressed on field
     */
    private void onMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            Bounds boundsInScene = this.localToScene(this.getBoundsInLocal());
            if (this.image != null) {
                dragDeltaX = boundsInScene.getMinX();
                dragDeltaY = boundsInScene.getMinY();
                this.setTranslateX(e.getSceneX() - dragDeltaX - board.squareSize / 2);
                this.setTranslateY(e.getSceneY() - dragDeltaY - board.squareSize / 2);
                toFront();
            }

            if (image != null) {
                board.highlightSquare(Square.fromValue(Character.toString('A' + x) + (y + 1)));
            }

            if (!(toX < 0 || toX > 7 || toY < 0 || toY > 7)) {
                board.outlineSquares[Square.fromValue(Character.toString('A' + toX) + (1 + toY)).ordinal()].setVisible(false);
            }
            boundsInScene = board.position.localToScene(this.getBoundsInLocal());
            if (board.userSide.equals(Side.BLACK)) {
                toX = 7 - (int) Math.floor((e.getSceneX() - boundsInScene.getMinX()) / board.squareSize);
                toY = (int) Math.floor((e.getSceneY() - boundsInScene.getMinY()) / board.squareSize);
            } else {
                toX = (int) Math.floor((e.getSceneX() - boundsInScene.getMinX()) / board.squareSize);
                toY = 7 - (int) Math.floor((e.getSceneY() - boundsInScene.getMinY()) / board.squareSize);
            }
            if (!(toX < 0 || toX > 7 || toY < 0 || toY > 7)) {
                board.outlineSquares[Square.fromValue(Character.toString('A' + toX) + (1 + toY)).ordinal()].setVisible(true);
            }
        }
        e.consume();
    }

    /**
     * Callback when mouse is dragged on field
     */
    private void onMouseDragged(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            this.setTranslateX(e.getSceneX() - dragDeltaX - board.squareSize / 2);
            this.setTranslateY(e.getSceneY() - dragDeltaY - board.squareSize / 2);

            if (!(toX < 0 || toX > 7 || toY < 0 || toY > 7)) {
                board.outlineSquares[Square.fromValue(Character.toString('A' + toX) + (1 + toY)).ordinal()].setVisible(false);
            }
            Bounds boundsInScene = board.position.localToScene(this.getBoundsInLocal());
            if (board.userSide.equals(Side.BLACK)) {
                toX = 7 - (int) Math.floor((e.getSceneX() - boundsInScene.getMinX()) / board.squareSize);
                toY = (int) Math.floor((e.getSceneY() - boundsInScene.getMinY()) / board.squareSize);
            } else {
                toX = (int) Math.floor((e.getSceneX() - boundsInScene.getMinX()) / board.squareSize);
                toY = 7 - (int) Math.floor((e.getSceneY() - boundsInScene.getMinY()) / board.squareSize);
            }
            if (!(toX < 0 || toX > 7 || toY < 0 || toY > 7)) {
                board.outlineSquares[Square.fromValue(Character.toString('A' + toX) + (1 + toY)).ordinal()].setVisible(true);
            }
        }
    }

    /**
     * Callback when mouse is released on field.
     */
    private void onMouseReleased(MouseEvent e) {
        this.setTranslateX(0);
        this.setTranslateY(0);
        if (!(toX < 0 || toX > 7 || toY < 0 || toY > 7)) {
            board.outlineSquares[Square.fromValue(Character.toString('A' + toX) + (1 + toY)).ordinal()].setVisible(false);
        }
        board.unhighlightSquare(Square.fromValue(Character.toString('A' + x) + (1 + y)));
        if (e.getButton() == MouseButton.PRIMARY) {
            int offsetX, offsetY;
            String move;
            if (board.userSide.equals(Side.BLACK)) {
                offsetX = -(int) Math.floor((e.getSceneX() - dragDeltaX) / board.squareSize);
                offsetY = (int) Math.floor((e.getSceneY() - dragDeltaY) / board.squareSize);
            } else {
                offsetX = (int) Math.floor((e.getSceneX() - dragDeltaX) / board.squareSize);
                offsetY = -(int) Math.floor((e.getSceneY() - dragDeltaY) / board.squareSize);
            }

            if (board.getDropPieceSelected() != 0) {
                Bounds boundsInScene = board.position.localToScene(this.getBoundsInLocal());
                String to;
                if (board.userSide.equals(Side.BLACK)) {
                    toX = 7 - (int) Math.floor((e.getSceneX() - boundsInScene.getMinX()) / board.squareSize);
                    toY = (int) Math.floor((e.getSceneY() - boundsInScene.getMinY()) / board.squareSize);
                } else {
                    toX = (int) Math.floor((e.getSceneX() - boundsInScene.getMinX()) / board.squareSize);
                    toY = 7 - (int) Math.floor((e.getSceneY() - boundsInScene.getMinY()) / board.squareSize);
                }
                if (toX < 0 || toX > 7 || toY < 0 || toY > 7) {
                    e.consume();
                    return;
                }
                to = Character.toString('A' + toX) + (1 + toY);
                move = new char[]{'P', 'N', 'B', 'R', 'Q'}[board.getDropPieceSelected() - 1] + "@" + to;
            } else {
                toX = x + offsetX;
                toY = y + offsetY;
                if (toX < 0 || toX > 7 || toY < 0 || toY > 7) {
                    e.consume();
                    return;
                }
                String from = Character.toString('A' + x) + (1 + y);
                String to = Character.toString('A' + toX) + (1 + toY);
                if (to.equals(from)) { // Destination square can't be the same as origin square
                    e.consume();
                    return;
                }
                move = from + to;
            }
            board.position.doMove(move);
        }
        e.consume();
    }

    /**
     * Sets displayed image for this field.
     *
     * @param piece
     */
    public void setImage(Piece piece) {
        image = pieceToImage.get(piece);
        ImageView iv = new ImageView(image);
        iv.setFitHeight(board.squareSize);
        iv.setFitWidth(board.squareSize);
        setGraphic(iv);
    }
}
