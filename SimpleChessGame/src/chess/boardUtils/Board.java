package chess.boardUtils;

import chess.pieces.*;
import chess.pieces.Piece.COLOR;
import chess.pieces.piece_location.Location;
import chess.pieces.piece_move.Move;
import chess.players.Player;

import java.util.ArrayList;

public final class Board implements Cloneable {
    //excluding king as king cannot be captured
    private final Player player[];
    private int bCapturedX, bCapturedY;
    private int wCapturedX, wCapturedY;
    private int piecesAliveNumber, previousPiecesAliveNumber;
    private int StaticPawnNumber, previousStaticPawnNumber;
    private final Tile[][] tiles;
    private Piece capturedPiece;
    private final Piece[] bPieces, wPieces;
    private ArrayList<Move> highlightMove = new ArrayList<>();//SHOW the moves a piece can make on tile
    private Pawn enPassantPawn;
    private boolean canDisableEnpassant;
    private int movesMade;

    public static final int BOARD_ROW_NUM = 8, BOARD_COL_NUM = 8;

    public Board() {
        this.player = new Player[2];
        this.tiles = new Tile [8][8];
        this.bPieces = new Piece[16];
        this.wPieces = new Piece[16];
    }

    public Player[] getPlayers() {
        return this.player;
    }

    public Player getCurrentPlayer() {
        return this.player[0];
    }

    public Player[] swapPlayers() {
        this.player[0].setMyPieceToNull();
        this.player[1].setMyPieceToNull();
        final Player temp = this.player[0];
        this.player[0] = this.player[1];
        this.player[1] = temp;
        this.setOpponent();
        return this.player;
    }

    public void setOpponent() {
        this.player[0].setOpponent(this.player[1]);
        this.player[1].setOpponent(this.player[0]);
    }

    public void setCapturedPiece(final Piece capturedPiece) {
        this.capturedPiece = capturedPiece;
        if (capturedPiece.getColor().equals(COLOR.WHITE)) {
            final int x = this.wCapturedX;
            final int y = this.wCapturedY;
            this.capturedPiece.setLocation(new Location(x, y));
            this.capturedPiece.resetCoordinateForDisplay();
            if (x >= -3 && x < -1) {
                this.wCapturedX = x + 1;
            } else {
                this.wCapturedX = -3;
                this.wCapturedY ++;
            };
        } else {
            final int x = this.bCapturedX;
            final int y = this.bCapturedY;
            this.capturedPiece.setLocation(new Location(x, y));
            this.capturedPiece.resetCoordinateForDisplay();
            if (x >= 8 && x < 10) {
                this.bCapturedX = x + 1;
            } else {
                this.bCapturedX = 8;
                this.bCapturedY ++;
            };
        }
    }

    public void setPlayers() {
        player[0] = new Player();
        player[0].setMyTurn(true);
        player[1] = new Player();
        player[1].setMyTurn(false);
        this.setOpponent();
    }

    public Pawn getEnPassantPawn() {
        return this.enPassantPawn;
    }
    public boolean getCanDisableEnPassant() {
        return this.canDisableEnpassant;
    }

    public void storeEnPassantPawn(final Pawn pawn, final boolean canDisableEnpassant) {
        this.enPassantPawn = pawn;
        this.canDisableEnpassant = canDisableEnpassant;
    }
    public void disableEnPassantPawn() {
        this.enPassantPawn.disableEnPassant();
        this.enPassantPawn = null;
    }

    public void increasePieceAlive() {
        this.piecesAliveNumber++;
    }

    public void reducePieceAlive() {
        this.piecesAliveNumber--;
    }
    public void reduceStaticPawn() {
        this.StaticPawnNumber--;
    }

    public ArrayList<Move> getLocationHints() {
        return this.highlightMove;
    }

    public void setLocationHints(final ArrayList<Move> highlightMove) {
        this.highlightMove = highlightMove;
    }

    public Tile[][] getBoard() {
        return this.tiles;
    }

    public Tile getTileOnBoard(final Location location) {
        final int x = location.getX();
        final int y = location.getY();
        return this.tiles[y][x];
    }

    public void setWhitePiece(final Piece piece, final int index) {
        this.wPieces[index] = piece;
    }
    public Piece getWhitePieceAtIndex(final int index) {
        return this.wPieces[index];
    }
    public Piece[] getWhitePieces() {
        return this.wPieces;
    }

    public void setBlackPiece(final Piece piece, final int index) {
        this.bPieces[index] = piece;
    }
    public Piece getBlackPieceAtIndex(final int index) {
        return this.bPieces[index];
    }
    public Piece[] getBlackPieces() {
        return this.bPieces;
    }

    private void setUpPiece(final Piece piece[], final int y, final int OFFSET, final int kPos, final int qPos, final COLOR color) {
        final int pawnRowNUM = (y == 0) ? 1 : 6;
        for (int i = 0; i < 8; i++) {
            piece[i] = new Pawn(i, i, pawnRowNUM, OFFSET, color);
        }
        piece[8] = new Rook(8, 0, y, color);
        piece[9] = new Knight(9, 1, y, color);
        piece[10] = new Bishop(10, 2, y, color);
        piece[11] = new Queen(11, qPos, y, color);
        piece[12] = new King(12, kPos, y, OFFSET, color);
        piece[13] = new Bishop(13, 5, y, color);
        piece[14] = new Knight(14, 6, y, color);
        piece[15] = new Rook(15, 7, y, color);
    }

    public void startNewGame(final int y, final int OFFSET, final int kPos, final int qPos) {
        resetValue();
        setUpPiece(wPieces, 7 - y, OFFSET, kPos, qPos, COLOR.WHITE);
        setUpPiece(bPieces, y, -OFFSET, kPos, qPos, COLOR.BLACK);
        setBoard();
        setPlayers();
    }

    private void resetValue() {
        this.canDisableEnpassant = false;
        this.movesMade = 0;
        this.piecesAliveNumber = this.previousPiecesAliveNumber = 31;
        this.StaticPawnNumber = this.previousStaticPawnNumber = 8;
        this.bCapturedX = 8;
        this.bCapturedY = this.wCapturedY = 1;
        this.wCapturedX = -3;
    }

    private void setBoard() {
        int y = 0, x = 0;
        for (int i = 0; i < this.tiles.length; i++) {
            for (int j = 0; j < this.tiles[0].length; j++)
                this.tiles[i][j] = new Tile();
        }
        for (int i = 0; i < this.wPieces.length; i++) {
            if (this.wPieces[i].getPieceAlive()) {
                final Location location = this.wPieces[i].getLocation();
                y = location.getY();
                x = location.getX();
                this.tiles[y][x] = new Tile(this.wPieces[i]);
            }
            if (this.bPieces[i].getPieceAlive()) {
                final Location location = this.bPieces[i].getLocation();
                y = location.getY();
                x = location.getX();
                this.tiles[y][x] = new Tile(this.bPieces[i]);
            }
        }
    }

    public boolean noPawnMoved() {
        if (this.StaticPawnNumber == this.previousStaticPawnNumber) {
            return true;
        }
        this.previousStaticPawnNumber = this.StaticPawnNumber;
        return false;
    }

    public boolean noPieceCaptured() {
        if (this.piecesAliveNumber == this.previousPiecesAliveNumber) {
            return true;
        }
        this.previousPiecesAliveNumber = this.piecesAliveNumber;
        return false;
    }

    public boolean fiftyMoveDraw() {
        if (noPieceCaptured() && noPawnMoved())
            movesMade++;
        else movesMade = 0;
        if (movesMade == 100)
            return true;
        return false;
    }

    public boolean onlyHasKing() {
        for (int i = 0; i < bPieces.length; i++) {
            if (!(bPieces[i] instanceof King) && bPieces[i].getPieceAlive() || !(wPieces[i] instanceof King) && wPieces[i].getPieceAlive())
                return false;
        }
        return true;
    }

    @Override
    public Object clone(){
        try {
            return (Board)super.clone();
        } catch (final CloneNotSupportedException e) {
            return null;
        }
    }
}