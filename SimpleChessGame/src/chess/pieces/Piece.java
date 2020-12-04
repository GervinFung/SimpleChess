package chess.pieces;

import java.util.ArrayList;

import chess.boardUtils.Board;
import chess.pieces.piece_location.Location;
import chess.pieces.piece_move.Move;

public abstract class Piece{

    public enum COLOR {
        WHITE, BLACK
    }

    private boolean status, firstMove;
    private final int index, value;
    private int X_DISPLAY, Y_DISPLAY;
    private boolean onlyKingCanMove;
    private final COLOR color;
    private Location location;

    public Piece(final int index, final int x, final int y, final COLOR color, final int value, final boolean firstMove) {
        this.status = true;
        this.X_DISPLAY = x;
        this.Y_DISPLAY = y;
        this.index = index;
        this.location = new Location(x, y);
        this.color = color;
        this.value = value;
        this.firstMove = firstMove;
    }

    public final COLOR getColor() {
        return this.color;
    }

    public void setFirstMove(final boolean firstMove) {
        this.firstMove = firstMove;
    }

    public boolean isFirstMove() {
        return this.firstMove;
    }

    public int getValue() {
        return this.value;
    }

    public Location getLocation() {
        return this.location;
    }
    public void setLocation(final Location location) {
        this.location = location;
    }
    public final void setCoordinateForDisplay(final int x, final int y) {
        this.X_DISPLAY = x;
        this.Y_DISPLAY = y;
    };
    public final void resetCoordinateForDisplay() {
        this.X_DISPLAY = this.location.getX();
        this.Y_DISPLAY = this.location.getY();
    }
    public final int getX_ForDisplay() {
        return this.X_DISPLAY;
    };
    public final int getY_ForDisplay() {
        return this.Y_DISPLAY;
    };
    public final void setPieceStatus(final boolean status) {
        this.status = status;
    };
    public final boolean getPieceAlive() {
        return this.status;
    };

    public final int getIndex() {
        return this.index;
    };
    public final void setOnlyKingCanMove(final boolean kingMove) {
        this.onlyKingCanMove = kingMove;
    };
    public final boolean getOnlyKingCanMove() {
        return this.onlyKingCanMove;
    };

    public abstract ArrayList<Move> calculateAllLegalMoves(final Board board, final King king);

    public final static void onlyKingCanMove(final Piece[] pieces, final boolean onlyKingCanMove) {
        for (final Piece piece : pieces) {
            if (piece.getPieceAlive()) {
                piece.setOnlyKingCanMove(onlyKingCanMove);
            }
        }
    }

    public final boolean isWithinTile(final int toX, final int toY) {
        return (toX >= 0 && toX <= 7 && toY >= 0 && toY <= 7);
    }
}