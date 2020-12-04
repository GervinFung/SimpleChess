package chess.pieces.piece_move;

import java.util.ArrayList;

import chess.boardUtils.Board;
import chess.boardUtils.Tile;
import chess.pieces.King;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Rook;
import chess.pieces.piece_location.Location;

public class Move {

    protected final Board board;
    protected final Piece movePiece;
    protected final Location location, previous;
    protected final boolean isFirstMove;

    public static final ArrayList<Move> retainSameMove(final ArrayList<Move> moveList, final ArrayList<Move> checkKingMoveList) {
        final ArrayList<Move> finalMoveList = new ArrayList<>();
        for (final Move move : moveList) {

            for (final Move checkKingMove : checkKingMoveList) {

                if (move.getLocation().equals(checkKingMove.getLocation())) {
                    finalMoveList.add(move);
                }
            }
        }
        return finalMoveList;
    }


    public static Board boardBuilder(final Board board) {
        final Board buildBoard = (Board)board.clone();
        int y = 0, x = 0;
        for (int i = 0; i < Board.BOARD_ROW_NUM; i++) {
            for (int j = 0; j < Board.BOARD_COL_NUM; j++) {
                buildBoard.getBoard()[i][j] = new Tile();
            }
        }
        for (final Piece piece : buildBoard.getCurrentPlayer().getAllMyPieces()) {
            if (piece.getPieceAlive()) {
                final Location location = piece.getLocation();
                y = location.getY();
                x = location.getX();
                buildBoard.getBoard()[y][x] = new Tile(piece);
            }
        }
        for (final Piece piece : buildBoard.getCurrentPlayer().getOpponent().getAllMyPieces()) {
            if (piece.getPieceAlive()) {
                final Location location = piece.getLocation();
                y = location.getY();
                x = location.getX();
                buildBoard.getBoard()[y][x] = new Tile(piece);
            }
        }
        return buildBoard;
    }


    @Override
    public boolean equals(final Object object) {
        // if both the object references are
        // referring to the same object
        // In other word its comparing to itself
        if (object == this) {
            return true;
        }

        // if object is null
        // Move class will never be used to generate Move
        // if is same class it's false
        if (object == null || object.getClass() == this.getClass()) {
            return false;
        }
        // type cast object to Location to make comparison
        final Move move = (Move)object;

        return move.getClass() != this.getClass() && this.location.getX() == move.getLocation().getX() && this.location.getY() == move.getLocation().getY();
    }

    @Override
    public int hashCode() {
        return this.location.hashCode();
    }

    private Move(final Board board, final Piece movePiece, final Location previous, final Location location) {
        this.board = board;
        this.movePiece = movePiece;
        this.previous = previous;
        this.location = location;
        this.isFirstMove = movePiece.isFirstMove();
    }

    public Move(final Location location) {
        this.board = null;
        this.movePiece = null;
        this.previous = null;
        this.isFirstMove = false;
        this.location = location;
    }

    public void makeMove() {
        this.movePiece.setLocation(location);
        this.movePiece.setFirstMove(false);
        if (Pawn.isPawn(this.movePiece)) {
            final Pawn pawn = (Pawn)this.movePiece;
            pawn.increaseMoveCount();
        }
    }

    public void undoMove() {
        this.movePiece.setLocation(previous);
        System.out.println(previous.getX() + " " + previous.getY());
        this.movePiece.setFirstMove(this.isFirstMove);
        if (Pawn.isPawn(this.movePiece)) {
            final Pawn pawn = (Pawn)this.movePiece;
            pawn.decreaseMoveCount();
        }
    }

    public Piece getMovedPiece() {
        return this.movePiece;
    }

    public Location getLocation() {
        return this.location;
    }

    public static Board executeMove(final ArrayList<Move> moveList, final Move move) {
        final Move legalMove = getMove(moveList, move);
        if (legalMove != null) {
            legalMove.makeMove();
            //final Move copy = (Move)legalMove.clone();
            //copy.makeMove();
            return boardBuilder(legalMove.board);
        }
        return null;
    }
    
    public static Move getMove(final ArrayList<Move> moveList, final Move move) {
        for (final Move moves : moveList) {
            if (moves.equals(move)) {
                return moves;
            }
        }
        return null;
    }

    public static class NormalMove extends Move {

        public NormalMove(final Board board, final Piece movePiece, final Location previous, final Location location) {
            super(board, movePiece, previous, location);
        }
        @Override
        public boolean equals(final Object object) {
            return object == this || super.equals(object);
        }
    }

    public static class PawnLeap extends Move {
        public PawnLeap(final Board board, final Pawn movePawn, final Location previous, final Location location) {
            super(board, movePawn, previous, location);
        }

        @Override
        public void makeMove() {
            this.movePiece.setLocation(location);
            this.movePiece.setFirstMove(false);
            final Pawn pawn = (Pawn)this.movePiece;
            pawn.enableEnPassant();
            pawn.increaseMoveCountTwice();
        }

        @Override
        public void undoMove() {
            this.movePiece.setLocation(previous);
            this.movePiece.setFirstMove(this.isFirstMove);
            final Pawn pawn = (Pawn)this.movePiece;
            pawn.disableEnPassant();
            pawn.decreaseMoveCountTwice();
        }

        @Override
        public boolean equals(final Object object) {
            return object == this || super.equals(object);
        }
    }

    public static class AttackMove extends Move{

        private final Piece attackedPiece;
        public AttackMove(final Board board, final Piece movePiece, final Location previousLocation, final Piece attackedPiece, final Location location) {
            super(board, movePiece, previousLocation, location);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public void makeMove() {
            this.movePiece.setLocation(location);
            this.movePiece.setFirstMove(false);
            this.attackedPiece.setPieceStatus(false);
            board.setCapturedPiece(this.attackedPiece);
            board.reducePieceAlive();
            if (Pawn.isPawn(this.movePiece)) {
                final Pawn pawn = (Pawn)this.movePiece;
                pawn.increaseMoveCount();
            }
        }

        @Override
        public void undoMove() {
            this.movePiece.setLocation(location);
            this.movePiece.setFirstMove(this.isFirstMove);
            this.attackedPiece.setPieceStatus(true);
            board.increasePieceAlive();
            if (Pawn.isPawn(this.movePiece)) {
                final Pawn pawn = (Pawn)this.movePiece;
                pawn.decreaseMoveCount();
            }
        }

        @Override
        public boolean equals(final Object object) {
            return object == this || super.equals(object);
        }
    }

    public static class CastleMove extends Move {

        private final Rook castleRook;
        private final Location rookCastleLocation, previousRookCastleLocation;

        public CastleMove (final Board board, final King king, final Rook castleRook, final Location previous, final Location location, final Location previousRookCastleLocation, final Location rookCastleLocation) {
            super(board, king, previous, location);
            this.castleRook = castleRook;
            this.rookCastleLocation = rookCastleLocation;
            this.previousRookCastleLocation = previousRookCastleLocation;
            king.setHasCastled();
        }

        @Override
        public void makeMove() {
            this.movePiece.setLocation(location);
            this.movePiece.setFirstMove(false);
            this.castleRook.setLocation(this.rookCastleLocation);
            this.castleRook.resetCoordinateForDisplay();
            this.movePiece.setFirstMove(false);
        }

        @Override
        public void undoMove() {
            this.movePiece.setLocation(this.previous);
            this.movePiece.setFirstMove(true);
            this.castleRook.setLocation(this.previousRookCastleLocation);
            this.castleRook.resetCoordinateForDisplay();
            this.movePiece.setFirstMove(true);
        }

        @Override
        public boolean equals(final Object object) {
            return object == this || super.equals(object);
        }
    }
}