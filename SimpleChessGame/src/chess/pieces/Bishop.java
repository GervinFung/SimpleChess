package chess.pieces;

import chess.pieces.piece_move.InfiniteMove;
import chess.pieces.piece_move.Move;
import chess.boardUtils.Board;
import chess.pieces.piece_location.Location;

import static chess.pieces.piece_move.Move.*;

import java.util.ArrayList;

public final class Bishop extends Piece implements InfiniteMove{

    public Bishop(final int index, final int x, final int y, final COLOR color) {
        super(index, x, y, color, 300, true);
    }

    @Override
    public ArrayList<Move> calculateAllLegalMoves(final Board board, final King king) {
        final ArrayList<Move> moveList = new ArrayList<>();
        if (!this.getOnlyKingCanMove()) {
            for (int i = 0; i < 4; i++) {
                moveList.addAll(calculateLegalMoves(board, i, king));
            }
            if (king.getKingDirectCheckStatus()) {
                moveList.retainAll(king.getDirectCheckKingCoordinate());
            }
            if (king.getKingDiscoverCheckStatus()) {
                moveList.retainAll(king.getDiscoverCheckKingCoordinate());
            }
        }
        return moveList;
    }

    @Override
    public ArrayList<Move> calculateLegalMoves(final Board board, final int i, final King king) {
        int y = this.getLocation().getY(), x = this.getLocation().getX();
        final ArrayList<Move> moveList = new ArrayList<>();
        final Location oriLocation = this.getLocation();
        Location newPossibleLocation = null;
        do {
            if (i == 0) {
                y--;
                x--;
            }
            else if (i == 1) {
                y--;
                x++;
            }
            else if (i == 2) {
                y++;
                x--;
            }
            else if (i == 3) {
                y++;
                x++;
            }
            if (!this.isWithinTile(x, y)) {
                break;
            }
            final Location possibleLocation = new Location(x, y);
            if (!board.getTileOnBoard(possibleLocation).getTileOccupiedByOwnPiece(this)) {
                this.setLocation(possibleLocation);
                final boolean kingSafe = king.checkKingSafe(board, oriLocation, this, false);
                newPossibleLocation = Location.generatePossibleLocation(board, this, oriLocation, possibleLocation, kingSafe);
                if (newPossibleLocation == null) {
                    break;
                }
            }
            if (board.getTileOnBoard(possibleLocation).getTileOccupiedByOpponent(this)) {
                if (newPossibleLocation != null) {
                    moveList.add(new AttackMove(board, this, oriLocation, board.getTileOnBoard(newPossibleLocation).getPieceOnTile(), newPossibleLocation));
                }
                break;
            }
            else if (board.getTileOnBoard(possibleLocation).getTileNotOccupied()) {
                if (newPossibleLocation != null) {
                    moveList.add(new NormalMove(board, this, oriLocation, newPossibleLocation));
                }
            }
        } while (!board.getTileOnBoard(new Location(x, y)).getTileOccupiedByOwnPiece(this));
        return moveList;
    }
}