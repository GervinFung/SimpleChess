package chess.pieces;

import chess.boardUtils.Board;
import chess.pieces.piece_location.Location;
import chess.pieces.piece_move.FiniteMove;
import chess.pieces.piece_move.Move;

import static chess.pieces.piece_move.Move.*;

import java.util.ArrayList;

public final class Pawn extends Piece implements FiniteMove{
    public Pawn(final int index, final int x, final int y, final int OFFSET, final COLOR color) {
        super(index, x, y, color, 100, true);
        this.canBeEnPassant = false;
        this.OFFSET = OFFSET;
        this.moveCount = 0;
    }

    private boolean canBeEnPassant;
    private final int OFFSET;
    private int moveCount;

    public int getOFFSET() {
        return this.OFFSET;
    };

    public void decreaseMoveCount() {
        if (this.moveCount > 0) {
            this.moveCount -= 1;
        }
    }

    public void decreaseMoveCountTwice() {
        if (this.moveCount > 0) {
            this.moveCount -= 2;
        }
    }

    public void increaseMoveCount() {
        this.moveCount += 1;
    }

    public void increaseMoveCountTwice() {
        this.moveCount += 2;
    }

    public int getMoveCount() {
        return this.moveCount;
    }

    public boolean getCanPromote() {
        return this.moveCount == 6;
    }

    public void disableEnPassant() {
        this.canBeEnPassant = false;
    }

    public void enableEnPassant() {
        this.canBeEnPassant = true;
    }

    public boolean getCanBeEnPassant() {
        return this.canBeEnPassant;
    }

    @Override
    public Move calculateLegalMoves(final Board board, final int i, final King king) {
        int y = this.getLocation().getY(), x = this.getLocation().getX();
        final Location oriLocation = this.getLocation();
        Location newLocation = null;
        Move newMove = null;
        //LEAP forward 2 tiles
        //&& (y == 6 || y == 1) && this.isWithinTile(x, y + 2 * -this.getOFFSET())
        if (i == 0 && this.isFirstMove()) {
            y = y + 2 * -this.getOFFSET();
            final Location possibleLocation = new Location(x, y);
            final Location nextLocation = new Location(x, y + 1*this.getOFFSET());
            if (board.getTileOnBoard(possibleLocation).getTileNotOccupied() && board.getTileOnBoard(nextLocation).getTileNotOccupied()) {
                this.setLocation(possibleLocation);
                final boolean kingSafe = king.checkKingSafe(board, oriLocation, this, false);
                newLocation = Location.generatePossibleLocation(board, this, oriLocation, possibleLocation, kingSafe);
                if (newLocation != null) {
                    newMove = new PawnLeap(board, this, oriLocation, newLocation);
                }
            }
        }
        //LEAP forward 1 tile
        else if (i == 1) {
            y = y + -this.getOFFSET();
            final Location possibleLocation = new Location(x, y);
            if (this.isWithinTile(x, y) && board.getTileOnBoard(possibleLocation).getTileNotOccupied()) {
                this.setLocation(possibleLocation);
                final boolean kingSafe = king.checkKingSafe(board, oriLocation, this, false);
                newLocation = Location.generatePossibleLocation(board, this, oriLocation, possibleLocation, kingSafe);
                if (newLocation != null) {
                    newMove = new NormalMove(board, this, oriLocation, newLocation);
                }
            }
        }//left capture
        else if (i == 2) {
            newLocation = addCaptureMove(board, oriLocation, king, x, y, -1);
            if (newLocation != null) {
                newMove = new AttackMove(board, this, oriLocation, board.getTileOnBoard(newLocation).getPieceOnTile(), newLocation);
            }
        }
        //right capture
        else if (i == 3) {
            newLocation = addCaptureMove(board, oriLocation, king, x, y, 1);
            if (newLocation != null) {
                newMove = new AttackMove(board, this, oriLocation, board.getTileOnBoard(newLocation).getPieceOnTile(), newLocation);
            }
        }
        //left enpassant
        else if (i == 4) {
            newLocation = addEnPassantMove(board, oriLocation, king, x, y, -1);
            final Location pawnLocation = new Location(x + this.getOFFSET() * -1, y);
            if (newLocation != null) {
                newMove = new AttackMove(board, this, oriLocation, board.getTileOnBoard(pawnLocation).getPieceOnTile(), newLocation);
            }
        }
        //right enpassant
        else if (i == 5) {
            newLocation = addEnPassantMove(board, oriLocation, king, x, y, 1);
            final Location pawnLocation = new Location(x + this.getOFFSET() * 1, y);
            if (newLocation != null) {
                newMove = new AttackMove(board, this, oriLocation, board.getTileOnBoard(pawnLocation).getPieceOnTile(), newLocation);
            }
        }
        return newMove;
    }

    @Override
    public ArrayList<Move> calculateAllLegalMoves(final Board board, final King king) {
        final ArrayList<Move> moveList = new ArrayList<>();
        if (!this.getOnlyKingCanMove()) {
            for (int i = 0; i < 6; i++) {
                final Move move = calculateLegalMoves(board, i, king);
                if (move != null) {
                    moveList.add(move);
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

    private Location addCaptureMove(final Board board, final Location oriLocation, final King king, int x, int y, final int POS) {
        y = y + -this.getOFFSET();
        x = x + this.getOFFSET() * POS;
        final Location possibleLocation = new Location(x, y);
        if (this.isWithinTile(x, y) && board.getTileOnBoard(possibleLocation).getTileOccupiedByOpponent(this)) {
            this.setLocation(possibleLocation);
            final boolean kingSafe = king.checkKingSafe(board, oriLocation, this, false);
            return Location.generatePossibleLocation(board, this, oriLocation, possibleLocation, kingSafe);
        }
        return null;
    }

    private Location addEnPassantMove(final Board board, final Location oriLocation, final King king, int x, int y, final int POS) {
        final int temp = y;
        y = y + -this.getOFFSET();
        x = x + this.getOFFSET() * POS;
        final Location possibleLocation = new Location(x, y);
        final Location pawnLocation = new Location(x, temp);
        if (this.isWithinTile(x, y) && board.getTileOnBoard(pawnLocation).getTileOccupiedByOpponentPawn(this)) {
            final Piece enemyPiece = board.getTileOnBoard(pawnLocation).getPieceOnTile();
            final Pawn enemyPawn = (Pawn)enemyPiece;
            if (enemyPawn.getCanBeEnPassant()) {
                this.setLocation(possibleLocation);
                final boolean kingSafe = king.checkKingSafe(board, oriLocation, this, false);
                return Location.generatePossibleLocation(board, this, oriLocation, possibleLocation, kingSafe);
            }
        }
        return null;
    }

    public static boolean isPawn(final Piece piece) {
        return piece != null && piece instanceof Pawn;
    }
}