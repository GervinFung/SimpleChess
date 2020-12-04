package chess.pieces.piece_location;

import chess.boardUtils.Board;
import chess.pieces.Piece;

public final class Location {
    public Location(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    private final int x, y;

    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    @Override
    public boolean equals(final Object object) {
        // if both the object references are
        // referring to the same object
        // In other word its comparing to itself
        if (object == this) {
            return true;
        }

        // if object is null or is not of the same class
        // !(object instanceof Location) should be used sparingly??
        if (object == null || object.getClass()!= this.getClass()) {
            return false;
        }
        // type cast object to Location to make comparison
        final Location location = (Location) object;

        return this.x == location.x && this.y == location.y;
    }
    @Override
    public int hashCode() {
        //so that hash code is based on the y and x value
        return this.y * 10 + this.x;
    }

    public static Location generatePossibleLocation(final Board board, final Piece piece, final Location oriLocation, final Location possibleLocation, final boolean kingSafe) {
        Location newLocation = null;
        if (kingSafe) {
            newLocation = possibleLocation;
        }
        piece.setLocation(oriLocation);
        return newLocation;
    }
}
