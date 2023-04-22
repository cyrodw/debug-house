package com.github.cyrobw.debughouse;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.HashMap;

public class BughouseBoard {

    public enum MoveType {
        NORMAL,
        PREMOVE,
        EXECUTED_PREMOVE,
        REPLAYED_PREMOVE,
    }

    // Actual pieces in hand
    private HashMap<Piece, Integer> whiteHand;
    private HashMap<Piece, Integer> blackHand;
    // To accurately display pocket counts for premoves
    private HashMap<Piece, Integer> whiteHandOffset;
    private HashMap<Piece, Integer> blackHandOffset;

    // board state
    private Board board;


    public BughouseBoard() {
        blackHand = new HashMap<>();
        whiteHand = new HashMap<>();
        whiteHandOffset = new HashMap<>();
        blackHandOffset = new HashMap<>();
        reset();
    }

    /**
     * Reset board state
     */
    public void reset() {
        board = new Board();
        resetHand(Side.WHITE);
        resetHand(Side.BLACK);
        resetHandOffset(Side.WHITE);
        resetHandOffset(Side.BLACK);
    }

    /**
     * Sets virtual hand to actual hand
     *
     * @param side
     */
    public void resetHandOffset(Side side) {
        if (side.equals(Side.WHITE)) {
            for (Piece piece : Piece.allPieces) {
                whiteHandOffset.put(piece, 0);
            }
        } else {
            for (Piece piece : Piece.allPieces) {
                blackHandOffset.put(piece, 0);
            }
        }
    }

    /**
     * Resets offsets, usually called when premoves are cancelled
     */
    public void resetHandOffsets() {
        resetHandOffset(Side.WHITE);
        resetHandOffset(Side.BLACK);
    }

    /**
     * Resets the pocket pieces.
     *
     * @param side
     */
    public void resetHand(Side side) {
        if (side.equals(Side.WHITE)) {
            for (Piece piece : Piece.allPieces) {
                whiteHand.put(piece, 0);
            }
        } else {
            for (Piece piece : Piece.allPieces) {
                blackHand.put(piece, 0);
            }
        }
    }

    /**
     * Return the displayed pocket piece counts which are adjusted for premoves
     *
     * @param side
     */
    public HashMap<Piece, Integer> getDisplayedHand(Side side) {
        HashMap<Piece, Integer> hand = new HashMap<>();
        if (side.equals(Side.WHITE)) {
            for (Piece piece : Piece.allPieces) {
                hand.put(piece, whiteHand.get(piece) + whiteHandOffset.get(piece));
            }
        } else {
            for (Piece piece : Piece.allPieces) {
                hand.put(piece, blackHand.get(piece) + blackHandOffset.get(piece));
            }
        }
        return hand;
    }

    /**
     * Given a string of current pieces in hand set hand.
     *
     * @param hand
     * @param side
     */
    public void setHand(String hand, Side side) {
        resetHand(side);
        char[] pieces = hand.toCharArray();
        for (char p : pieces) {
            Piece piece = Piece.fromFenSymbol(p + "");
            if (side.equals(Side.WHITE)) {
                whiteHand.put(piece, whiteHand.get(piece) + 1);
            } else {
                blackHand.put(piece, blackHand.get(piece) + 1);
            }
        }
    }

    /**
     * Add piece back to hand offset.
     * Called when a premove drop has been successfully executed.
     *
     * @param piece
     * @param side
     */
    public void addToHandOffset(Piece piece, Side side) {
        if (side.equals(Side.WHITE)) {
            whiteHandOffset.put(piece, whiteHandOffset.get(piece) + 1);
        } else {
            blackHandOffset.put(piece, blackHandOffset.get(piece) + 1);
        }
    }

    /**
     * Subtract piece from hand offset.
     * Called when a premove drop is made.
     *
     * @param piece
     * @param side
     */
    public void subtractFromHandOffset(Piece piece, Side side) {
        if (side.equals(Side.WHITE)) {
            whiteHandOffset.put(piece, whiteHandOffset.get(piece) - 1);
        } else {
            blackHandOffset.put(piece, blackHandOffset.get(piece) - 1);
        }
    }

    /**
     * @param piece
     * @param side
     */
    public void subtractFromHand(Piece piece, Side side) {
        if (side.equals(Side.WHITE)) {
            whiteHand.put(piece, whiteHand.get(piece) - 1);
        } else {
            blackHand.put(piece, blackHand.get(piece) - 1);
        }
    }

    /**
     * @return
     */
    public Side sideToMove() {
        return board.getSideToMove();
    }

    /**
     * @param fen
     */
    public void loadFromFen(String fen) {
        board.loadFromFen(fen);
    }

    /**
     * @return
     */
    public String getFen() {
        return board.getFen();
    }

    /**
     * @param sq
     * @return
     */
    public Piece getPiece(Square sq) {
        return board.getPiece(sq);
    }

    /**
     * @param file
     * @param rank
     * @return piece on square
     */
    public Piece getPiece(int file, int rank) {
        Square sq = Square.fromValue(Character.toString('A' + file) + (rank + 1));
        return board.getPiece(sq);
    }

    /**
     * Checks if given move is a legal move
     *
     * @param move
     */
    public boolean isLegal(String move) {
        Side side = board.getSideToMove();
        if (move.charAt(1) == '@') {
            Piece drop;
            if (side.equals(Side.WHITE)) {
                drop = Piece.fromFenSymbol(Character.toUpperCase(move.charAt(0)) + "");
                if (whiteHand.get(drop) <= 0) {
                    return false;
                }
            } else {
                drop = Piece.fromFenSymbol(Character.toLowerCase(move.charAt(0)) + "");
                if (blackHand.get(drop) <= 0) {
                    return false;
                }
            }
            Piece piece = board.getPiece(Square.fromValue(move.substring(2, 4).toUpperCase()));
            if (piece != Piece.NONE) {
                return false;
            }
            boolean b = move.charAt(3) == '1' || move.charAt(3) == '8';
            if (side.equals(Side.WHITE) && drop.equals(Piece.WHITE_PAWN) && b) {
                return false;
            }
            if (side.equals(Side.BLACK) && drop.equals(Piece.BLACK_PAWN) && b) {
                return false;
            }
            Square to = Square.fromValue(move.substring(2, 4).toUpperCase());
            board.setPiece(drop, to);
            boolean kingAttacked = board.isKingAttacked();
            board.unsetPiece(drop, to);
            if (kingAttacked) {
                return false;
            }
            return true;
        } else {
            var moves = board.pseudoLegalMoves();
            move = move.toLowerCase();
            for (Move m : moves) {
                if (m.toString().equals(move)) {
                    return board.isMoveLegal(m, true);
                }
            }
        }
        return false;
    }

    /**
     * Checks if given move is a valid premove which is any possible response.
     * This function is always called with opponent on turn.
     *
     * @param move - the premove
     */
    public boolean isValidPremove(String move) {
        Side side = board.getSideToMove();
        Square to = Square.fromValue(move.substring(2, 4).toUpperCase());
        if (move.charAt(1) == '@') {
            Piece drop;
            if (side.equals(Side.WHITE)) {
                drop = Piece.fromFenSymbol(Character.toLowerCase(move.charAt(0)) + "");
            } else {
                drop = Piece.fromFenSymbol(Character.toUpperCase(move.charAt(0)) + "");
            }
            // Can't drop on first or last rank
            return !drop.getPieceType().equals(PieceType.PAWN) || (move.charAt(3) != '1' && move.charAt(3) != '8');
        } else {
            Square from = Square.fromValue(move.substring(0, 2).toUpperCase());
            PieceType type = board.getPiece(from).getPieceType();
            if (type == null) {
                return false;
            }
            long attacks;
            switch (type) {
                case PAWN -> {
                    attacks = Bitboard.getPawnAttacks(side.flip(), from); // Flip side to get correct direction of pawns
                    if ((attacks & to.getBitboard()) != 0) {
                        return true;
                    }
                    attacks = Bitboard.getPawnMoves(side.flip(), from, 0);
                    if ((attacks & to.getBitboard()) != 0) {
                        return true;
                    }
                }
                case KNIGHT -> {
                    attacks = Bitboard.getKnightAttacks(from, ~0); // ~0 indicates can be attacked from all squares
                    if ((attacks & to.getBitboard()) != 0) {
                        return true;
                    }
                }
                case BISHOP -> {
                    attacks = Bitboard.getBishopAttacks(0, from);
                    if ((attacks & to.getBitboard()) != 0) {
                        return true;
                    }
                }
                case ROOK -> {
                    attacks = Bitboard.getRookAttacks(0, from);
                    if ((attacks & to.getBitboard()) != 0) {
                        return true;
                    }
                }
                case QUEEN -> {
                    attacks = Bitboard.getQueenAttacks(0, from);
                    if ((attacks & to.getBitboard()) != 0) {
                        return true;
                    }
                }
                case KING -> {
                    attacks = Bitboard.getKingAttacks(from, ~0);
                    if ((attacks & to.getBitboard()) != 0) {
                        return true;
                    }
                }
            }
            // Castling moves
            if (side == Side.WHITE) {
                CastleRight castleRight = board.getCastleRight(Side.BLACK);
                if (from.equals(Square.E8) && to.equals(Square.G8) &&
                        (castleRight.equals(CastleRight.KING_SIDE) || castleRight.equals(CastleRight.KING_AND_QUEEN_SIDE))) {
                    return true;
                }
                if (from.equals(Square.E8) && to.equals(Square.C8) &&
                        (castleRight.equals(CastleRight.QUEEN_SIDE) || castleRight.equals(CastleRight.KING_AND_QUEEN_SIDE))) {
                    return true;
                }
            } else {
                CastleRight castleRight = board.getCastleRight(Side.WHITE);
                if (from.equals(Square.E1) && to.equals(Square.G1) &&
                        (castleRight.equals(CastleRight.KING_SIDE) || castleRight.equals(CastleRight.KING_AND_QUEEN_SIDE))) {
                    return true;
                }
                if (from.equals(Square.E1) && to.equals(Square.C1) &&
                        (castleRight.equals(CastleRight.QUEEN_SIDE) || castleRight.equals(CastleRight.KING_AND_QUEEN_SIDE))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a move is a valid predrop.
     * A predrop is defined as a premove which is a drop but
     * AND (important), you don't have the piece yet.
     *
     * @param move
     * @param side
     */
    public boolean isPredrop(String move, Side side) {
        if (move.charAt(1) != '@') { // Drop
            return false;
        }
        Piece drop;
        if (side.equals(Side.WHITE)) {
            drop = Piece.fromFenSymbol(Character.toUpperCase(move.charAt(0)) + "");
            if (whiteHand.get(drop) > 0) { // Can't have the piece yet
                return false;
            }
        } else {
            drop = Piece.fromFenSymbol(Character.toLowerCase(move.charAt(0)) + "");
            if (blackHand.get(drop) > 0) { // Can't have the piece yet
                return false;
            }
        }
        return true;
    }

    /**
     * Perform move
     *
     * @param move - Move
     * @param type - MoveType
     */
    public void doMove(String move, MoveType type, Side userSide) {
        Side side = board.getSideToMove();
        board.setSideToMove(userSide);
        Square to = Square.fromValue(move.substring(2, 4).toUpperCase());
        if (move.charAt(1) == '@') {
            Piece drop;
            if (userSide.equals(Side.WHITE)) {
                drop = Piece.fromFenSymbol(Character.toUpperCase(move.charAt(0)) + "");
                if (type.equals(MoveType.NORMAL)) {
                    subtractFromHand(drop, Side.WHITE);
                } else if (type.equals(MoveType.PREMOVE)) {
                    subtractFromHandOffset(drop, Side.WHITE);
                } else if (type.equals(MoveType.EXECUTED_PREMOVE)) {
                    subtractFromHand(drop, Side.WHITE);
                    addToHandOffset(drop, Side.WHITE);
                }
            } else {
                drop = Piece.fromFenSymbol(Character.toLowerCase(move.charAt(0)) + "");
                if (type.equals(MoveType.NORMAL)) {
                    subtractFromHand(drop, Side.BLACK);
                } else if (type.equals(MoveType.PREMOVE)) {
                    subtractFromHandOffset(drop, Side.BLACK);
                } else if (type.equals(MoveType.EXECUTED_PREMOVE)) {
                    subtractFromHand(drop, Side.BLACK);
                    addToHandOffset(drop, Side.BLACK);
                }
            }
            putPiece(drop, to);
        } else {
            Square from = Square.fromValue(move.substring(0, 2).toUpperCase());
            if (move.length() == 5) {
                Piece promotionPiece;
                if (userSide.equals(Side.WHITE)) {
                    promotionPiece = Piece.fromFenSymbol(Character.toUpperCase(move.charAt(4)) + "");
                } else {
                    promotionPiece = Piece.fromFenSymbol(Character.toLowerCase(move.charAt(4)) + "");
                }
                board.doMove(new Move(from, to, promotionPiece));
            } else {
                board.doMove(new Move(from, to));
            }
        }
        if (type.equals(MoveType.NORMAL) || type.equals(MoveType.EXECUTED_PREMOVE)) {
            board.setSideToMove(side.flip());
        } else if (type.equals(MoveType.PREMOVE) || type.equals(MoveType.REPLAYED_PREMOVE)) {
            board.setSideToMove(side);
        }
    }

    /**
     * Places piece on a given square
     *
     * @param piece
     * @param sq
     */
    private void putPiece(Piece piece, Square sq) {
        Piece occupied = board.getPiece(sq);
        if (!occupied.equals(Piece.NONE)) {
            board.unsetPiece(occupied, sq);
        }
        board.setPiece(piece, sq);
    }
}