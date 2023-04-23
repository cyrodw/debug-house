package com.github.cyrodw.debughouse.ui;

import com.github.cyrodw.debughouse.BughouseBoard;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.LinkedList;

public class Position extends GridPane {
    private final BoardField[] fields = new BoardField[64]; // Each square corresponds to a field
    private final Board board;
    private LinkedList<String> premoves = new LinkedList<>();
    private LinkedList<Boolean> predrops = new LinkedList<>();

    public Position(Board board) {
        this.board = board;
        createFields();
        setOnMouseClicked(this::onMouseClicked);
    }

    /**
     * Attempt to trigger a premove and redisplay all the premoves on board.
     */
    public void executePremoves() {
        assert premoves.size() == predrops.size();

        if (!premoves.isEmpty() && board.gameState.sideToMove().equals(board.userSide)) { // Attempt to trigger premove
            if (Boolean.TRUE.equals(predrops.peek())) { // First premove is a predrop
                for (int i = 0; i < premoves.size(); i++) {
                    if (predrops.get(i) && board.gameState.isLegal(premoves.get(i))) {
                        board.gameState.doMove(premoves.get(i), BughouseBoard.MoveType.EXECUTED_PREMOVE, board.userSide);
                        board.pushMove(premoves.get(i));
                        for (int j = 0; j <= i; j++) {
                            premoves.remove(); // Remove all premoves before and including executed predrop
                            predrops.remove();
                        }
                        break;
                    }
                }
            } else {
                // Trigger first premove + all invalid premoves fall through
                while (!premoves.isEmpty()) {
                    String premove = premoves.remove();
                    Boolean predrop = predrops.remove();
                    if (board.gameState.isLegal(premove)) {
                        board.gameState.doMove(premove, BughouseBoard.MoveType.EXECUTED_PREMOVE, board.userSide);
                        board.pushMove(premove);
                        break;
                    } else if (predrop) {
                        premoves.addFirst(premove); // Re-add predrop since predrops are blocking
                        predrops.addFirst(true);
                        break;
                    }
                }
            }
        }
        board.highlightLastMove(board.getLastMove());

        // Replay remaining premoves
        for (String move : premoves) {
            board.gameState.doMove(move, BughouseBoard.MoveType.REPLAYED_PREMOVE, board.userSide);
            board.highlightPremove(move);
        }
    }

    /**
     * Cancels the current premoves.
     */
    public void cancelPremoves() {
        Client.sendToServer("cancel");
        board.setSelectedDrop(null);
        board.gameState.resetHandOffsets();
        board.gameState.loadFromFen(board.fen);
        premoves.clear();
        predrops.clear();
        board.unhighlightAll();
        board.highlightLastMove(board.getLastMove());
    }

    /**
     * Right click cancels premoves
     *
     * @param e the mouse event
     */
    private void onMouseClicked(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            cancelPremoves();
            render();
            board.renderHands();
        }
    }


    /**
     * Execute move on board.
     *
     * @param move
     */
    public void doMove(String move) {
        if (!board.isPlaying()) {
            return;
        }
        if (move.charAt(1) != '@' && move.length() < 5) { // Add promotion piece if promotion piece not set yet
            Piece from = board.gameState.getPiece(Square.valueOf(move.substring(0, 2).toUpperCase()));
            if (!from.equals(Piece.NONE)
                    && from.getPieceType().equals(PieceType.PAWN)
                    && (move.charAt(3) == '1' || move.charAt(3) == '8')) {
                if (board.underPromote) {
                    move += "n";
                } else {
                    move += "q";
                }
            }
        }
        boolean isPredrop = board.gameState.isPredrop(move, board.userSide);
        if (!premoves.isEmpty()
                || (!board.userSide.equals(board.gameState.sideToMove()) && board.gameState.isValidPremove(move))
                || isPredrop) { // Potential premove
            if (board.userBoard) {
                Client.sendToServer("premove " + move.toLowerCase() + " " + isPredrop);
                board.highlightPremove(move);
                board.gameState.doMove(move, BughouseBoard.MoveType.PREMOVE, board.userSide);
                premoves.add(move);
                predrops.add(isPredrop);
            } else {
                Client.sendToChat("premove " + board.gameState.getSan(move)); // Premove suggestion
            }
        } else if (board.gameState.isLegal(move)) {
            if (board.userBoard) {
                Client.sendToServer("move " + move.toLowerCase());
                board.unhighlightAll();
                board.highlightLastMove(move);
                board.gameState.doMove(move, BughouseBoard.MoveType.NORMAL, board.userSide);
                board.fen = board.gameState.getFen();
                board.updateClockTurns();
            } else {
                Client.sendToChat(board.gameState.getSan(move)); // Move suggestion
            }
        }
        if (board.userBoard) {
            board.renderHands();
            render();
        }
    }

    /**
     * Populates the board with 64 fields for each square.
     */
    public void createFields() {
        for (int i = 0; i < 64; i++) {
            int x = i % 8;
            int y = i / 8;
            BoardField square = new BoardField(board, x, y);
            fields[i] = square;
            if (board.userSide.equals(Side.BLACK)) {
                add(fields[i], 8 - x, y);
            } else {
                add(fields[i], x, 8 - y);
            }
        }
    }


    /**
     * Render current pieces on board.
     */
    public void render() {
        for (int i = 0; i < 64; i++) {
            int x = i % 8;
            int y = i / 8;
            Piece piece = board.gameState.getPiece(x, y);
            if (piece != Piece.NONE) {
                fields[i].setImage(piece);
            } else {
                fields[i].setImage(null);
            }
        }
    }
}
