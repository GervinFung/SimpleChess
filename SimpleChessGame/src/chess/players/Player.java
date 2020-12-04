package chess.players;

import chess.pieces.King;
import chess.pieces.Piece;

import chess.boardUtils.Board;
import chess.pieces.piece_location.Location;

public final class Player{

    private Piece[] myPieces;
    private Piece myPiece;
    private boolean myTurn;
    private Player opponent;

    public void setOpponent(final Player opponent) {
        this.opponent = opponent;
    }

    public Player getOpponent() {
        return this.opponent;
    }

    public void setAllMyPieces(final Piece[] myPieces) {
        this.myPieces = myPieces;
    }
    public Piece[] getAllMyPieces() {
        return this.myPieces;
    }

    public void promoteMyPawn(final Piece promotedPiece) {
        final int index = promotedPiece.getIndex();
        this.myPieces[index] = promotedPiece;
        this.myPiece = this.myPieces[index];
    }

    public Piece getMyPiece() {
        return this.myPiece;
    }
    public void setMyTurn(final boolean myTurn) {
        this.myTurn = myTurn;
    }
    public boolean getMyTurn() {
        return this.myTurn;
    }
    public void setMyPieceToNull() {
        this.myPiece = null;
    }
    public void setMyPiece(final Piece myPiece) {
        this.myPiece = myPiece;
    }
    public King getMyKing() {
        return (King)this.myPieces[12];
    }

    public void findMyPiece(final Board board, final Location location) {
        if (!board.getTileOnBoard(location).getTileNotOccupied()) {
            final Piece tempPiece = board.getTileOnBoard(location).getPieceOnTile();
            for (final Piece piece : this.getAllMyPieces()) {
                if (tempPiece == piece) {
                    this.setMyPiece(tempPiece);
                }
            }
        }
    }

    private boolean noMovesAvaliable(final Board board) {
        for (final Piece capturedPiece : this.getAllMyPieces()) {
            if (capturedPiece.getPieceAlive()) {
                if (!capturedPiece.calculateAllLegalMoves(board, this.getMyKing()).isEmpty())
                    return false;
            }
        }
        return true;
    }

    public boolean checkMate(final Board board) {
        return noMovesAvaliable(board) && 
        (this.getMyKing().getKingDirectCheckStatus() || this.getMyKing().getKingDiscoverCheckStatus());
    }

    public boolean staleMate(final Board board) {
        return noMovesAvaliable(board) &&
        !(this.getMyKing().getKingDirectCheckStatus() || this.getMyKing().getKingDiscoverCheckStatus());
    }
}