package chess.boardUtils;

import chess.pieces.*;

public final class Tile {

    private final Piece pieceOnTile;

    public Tile(final Piece pieceOnTile) {
        this.pieceOnTile = pieceOnTile;
    }

    public Tile() {
        this.pieceOnTile = null;
    }

    public final Piece getPieceOnTile() {
        return this.pieceOnTile;
    }

    public boolean getTileNotOccupied() {
        return this.pieceOnTile == null;
    }
    public boolean getTileOccupiedByOpponent(final Piece piece) {
        if (this.pieceOnTile != null) {
            if (!piece.getColor().equals(this.pieceOnTile.getColor())) {
                return true;
            }
        }
        return false;
    }
    public boolean getTileOccupiedByOwnPiece(final Piece piece) {
        if (this.pieceOnTile != null) {
            if (piece.getColor().equals(this.pieceOnTile.getColor())) {
                return true;
            }
        }
        return false;
    }
    public boolean getTileOccupiedByOpponentBishop(final Piece piece) {
        if (this.pieceOnTile != null) {
            if (!piece.getColor().equals(this.pieceOnTile.getColor()) && this.pieceOnTile instanceof Bishop) {
                return true;
            }
        }
        return false;
    }
    public boolean getTileOccupiedByOpponentKnight(final Piece piece) {
        if (this.pieceOnTile != null) {
            if (!piece.getColor().equals(this.pieceOnTile.getColor()) && this.pieceOnTile instanceof Knight) {
                return true;
            }
        }
        return false;
    }
    public boolean getTileOccupiedByOpponentPawn(final Piece piece) {
        if (this.pieceOnTile != null) {
            if (!piece.getColor().equals(this.pieceOnTile.getColor()) && this.pieceOnTile instanceof Pawn) {
                return true;
            }
        }
        return false;
    }
    public boolean getTileOccupiedByOpponentQueen(final Piece piece) {
        if (this.pieceOnTile != null) {
            if (!piece.getColor().equals(this.pieceOnTile.getColor()) && this.pieceOnTile instanceof Queen) {
                return true;
            }
        }
        return false;
    }
    public boolean getTileOccupiedByOpponentRook(final Piece piece) {
        if (this.pieceOnTile != null) {
            if (!piece.getColor().equals(this.pieceOnTile.getColor()) && this.pieceOnTile instanceof Rook) {
                return true;
            }
        }
        return false;
    }
    public boolean getTileOccupiedByOpponentKing(final Piece piece) {
        if (this.pieceOnTile != null) {
            if (!piece.getColor().equals(this.pieceOnTile.getColor()) && this.pieceOnTile instanceof King) {
                return true;
            }
        }
        return false;
    }
}