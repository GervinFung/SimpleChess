package chess.pieces;

import chess.pieces.piece_location.Location;
import chess.pieces.piece_move.FiniteMove;
import chess.pieces.piece_move.Move;
import chess.boardUtils.Board;

import static chess.pieces.piece_move.Move.*;

import java.util.ArrayList;

public final class Knight extends Piece implements FiniteMove{
    public Knight(final int index, final int x, final int y, final COLOR color) {
        super(index, x, y, color, 300, true);
    }

    @Override
    public ArrayList<Move> calculateAllLegalMoves(final Board board, final King king) {
        final ArrayList<Move> moveList = new ArrayList<>();
        if (!this.getOnlyKingCanMove()) {
            for (int i = 0; i < 8; i++) {
                final Move newMove = calculateLegalMoves(board, i, king);
                if (newMove != null) {
                    moveList.add(newMove);
                }
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
    public Move calculateLegalMoves(final Board board, final int i, final King king) {
        int y = this.getLocation().getY(), x = this.getLocation().getX();
        final Location oriLocation = this.getLocation();
        Location newLocation = null;
        Move newMove = null;
        if (i == 0) {
            y++;
            x+=2;
        } else if (i == 1) {
            y--;
            x+=2;
        } else if (i == 2) {
            y--;
            x-=2;
        } else if (i == 3) {
            y++;
            x-=2;
        } else if (i == 4) {
            y-=2;
            x++;
        } else if (i == 5) {
            y+=2;
            x++;
        } else if (i == 6) {
            y-=2;
            x--;
        } else if (i == 7) {
            y+=2;
            x--;
        }
        final Location possibleLocation = new Location(x, y);
        if (this.isWithinTile(x, y) && !board.getTileOnBoard(possibleLocation).getTileOccupiedByOwnPiece(this)) {
            this.setLocation(possibleLocation);
            final boolean kingSafe = king.checkKingSafe(board, oriLocation, this, false);
            newLocation = Location.generatePossibleLocation(board, this, oriLocation, possibleLocation, kingSafe);
            if (board.getTileOnBoard(possibleLocation).getTileOccupiedByOpponent(this)) {
                if (newLocation != null) {
                    newMove = new AttackMove(board, this, oriLocation, board.getTileOnBoard(newLocation).getPieceOnTile(), newLocation);
                }
            }
            else if (board.getTileOnBoard(possibleLocation).getTileNotOccupied()) {
                if (newLocation != null) {
                    newMove = new NormalMove(board, this, oriLocation, newLocation);
                }
            }
        }
        return newMove;
    }
}