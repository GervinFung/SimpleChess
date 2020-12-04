package chess.pieces;

import chess.pieces.piece_location.Location;
import chess.pieces.piece_move.FiniteMove;
import chess.pieces.piece_move.Move;
import chess.boardUtils.Board;

import static chess.pieces.piece_move.Move.*;

import java.util.ArrayList;

public final class King extends Piece implements FiniteMove {
    public King(final int index, final int x, final int y, final int OFFSET, final COLOR color) {
        super(index, x, y, color, 10000, true);
        this.kingInDirectCheck = this.kingInDiscoveredCheck = this.kingInDoubleCheck = this.hasCastled = false;
        this.kingSafe = true;
        this.OFFSET = OFFSET;
    }

    private boolean kingInDirectCheck, kingInDiscoveredCheck, kingSafe;
    private boolean kingInDoubleCheck = kingInDiscoveredCheck && kingInDirectCheck;
    private int discoverCheckYX;
    private final ArrayList<Move> directCheckCoordinate = new ArrayList<>(), discoverCheckCoordinate = new ArrayList<>();;
    private Piece directCheckPiece, discoverCheckPiece;
    private final int OFFSET;
    private boolean hasCastled;

    public void setHasCastled() {
        this.hasCastled = true;
    }

    public boolean getHasCastled() {
        return this.hasCastled;
    }

    public int getOFFSET() {
        return this.OFFSET;
    };

    public ArrayList<Move> getDirectCheckKingCoordinate() {
        return this.directCheckCoordinate;
    }

    public ArrayList<Move> getDiscoverCheckKingCoordinate() {
        return this.discoverCheckCoordinate;
    }

    public void setDirectCheckPiece(final Piece piece) {
        this.directCheckPiece = piece;
    }

    public void setDiscoverCheckPiece(final Board board, final Piece[] piece) {
        this.discoverCheckPiece = board
                .getTileOnBoard(new Location(this.discoverCheckYX % 10, this.discoverCheckYX / 10)).getPieceOnTile();
    }

    protected Piece getDiscoverCheckPiece() {
        return this.discoverCheckPiece;
    }

    protected Piece getDirectCheckPiece() {
        return this.directCheckPiece;
    }

    public boolean getKingDirectCheckStatus() {
        return this.kingInDirectCheck;
    }

    public boolean getKingDiscoverCheckStatus() {
        return this.kingInDiscoveredCheck;
    }

    public boolean getKingDoubleCheckStatus() {
        return this.kingInDoubleCheck;
    }

    public boolean getKingSafe() {
        return this.kingSafe;
    }

    @Override
    public Move calculateLegalMoves(final Board board, final int i, final King king) {
        int y = this.getLocation().getY(), x = this.getLocation().getX();
        final Location oriLocation = this.getLocation();
        Location newLocation = null;
        boolean kingSafe = false;
        Move newMove = null;
        if (i == 0) {
            y++;
        } else if (i == 1) {
            y--;
        } else if (i == 2) {
            x++;
        } else if (i == 3) {
            x--;
        } else if (i == 4) {
            y--;
            x--;
        } else if (i == 5) {
            y--;
            x++;
        } else if (i == 6) {
            y++;
            x--;
        } else if (i == 7) {
            y++;
            x++;
        }
        if (this.isWithinTile(x, y)) {
            final Location possibleLocation = new Location(x, y);
            if (!board.getTileOnBoard(possibleLocation).getTileOccupiedByOwnPiece(this)) {
                kingSafe = king.noThreat(board, possibleLocation);
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
        }
        return newMove;
    }

    @Override
    public ArrayList<Move> calculateAllLegalMoves(final Board board, final King king) {
        final ArrayList<Move> moveList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            final Move newMove = calculateLegalMoves(board, i, king);
            if (newMove != null) {
                moveList.add(newMove);
            }
        }
        if (this.isFirstMove() && !((this.kingInDirectCheck || this.kingInDiscoveredCheck || !this.kingSafe))) {
            final Location oriLocation = this.getLocation();
            final Location kingCastleRightLocation = addKingCastleNewLocation(board, -2, 0);
            if (kingCastleRightLocation != null) {
                final Piece piece = board.getTileOnBoard(new Location(0, this.getLocation().getY())).getPieceOnTile();
                final Location rookNewLocation = new Location(this.getLocation().getX() - 1, this.getLocation().getY());
                moveList.add(new CastleMove(board, this, (Rook)piece, oriLocation, kingCastleRightLocation, new Location(0, this.getLocation().getY()), rookNewLocation));
            }

            final Location kingCastleLeftLocation = addKingCastleNewLocation(board, 2, 7);
            if (kingCastleLeftLocation != null) {
                final Piece piece = board.getTileOnBoard(new Location(7, this.getLocation().getY())).getPieceOnTile();
                final Location rookNewLocation = new Location(this.getLocation().getX() + 1, this.getLocation().getY());
                moveList.add(new CastleMove(board, this, (Rook)piece, oriLocation, kingCastleLeftLocation, new Location(7, this.getLocation().getY()), rookNewLocation));
            }
        }
        return moveList;
    }

    // return new king location IF castle is possible
    private Location addKingCastleNewLocation(final Board board, final int offset, final int rookLocation) {
        final Piece piece = board.getTileOnBoard(new Location(rookLocation, this.getLocation().getY())).getPieceOnTile();

        if (piece != null && piece instanceof Rook) {

            final Rook rook = (Rook) piece;
            final int quantityLocation = findKingToRookDistance(rook);
            final Location[] emptyLocation = new Location[quantityLocation];

            int biggestX = Math.max(this.getLocation().getX(), rook.getLocation().getX());

            for (int i = 0; i < emptyLocation.length; i++) {
                biggestX--;
                emptyLocation[i] = new Location(biggestX, this.getLocation().getY());
            }
            if (canCastle(board, emptyLocation, rook)) {
                final Location newLocation = new Location(this.getLocation().getX() + offset,
                        this.getLocation().getY());
                return newLocation;
            }
        }
        return null;
    }

    private int findKingToRookDistance(final Rook rook) {
        final int ROOK_X = rook.getLocation().getX();
        final int KING_X = this.getLocation().getX();
        return Math.abs(KING_X - ROOK_X) - 1;
    }

    private boolean canCastle(final Board board, final Location[] emptyLocation, final Rook rook) {
        if (rook.isFirstMove()) {
            for (final Location location : emptyLocation) {
                if (!board.getTileOnBoard(location).getTileNotOccupied() || !this.noThreat(board, location)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean searchAllPawnThreat(final Board board, final Location location) {
        final int subtractor = this.getOFFSET();
        boolean noThreat = true;
        final int LOCATION_X = location.getX(), LOCATION_Y = location.getY();

        if (this.isWithinTile(LOCATION_X - subtractor, LOCATION_Y - subtractor)) {
            noThreat = !board.getTileOnBoard(new Location(LOCATION_X - subtractor, LOCATION_Y - subtractor))
                    .getTileOccupiedByOpponentPawn(this);
        } // check pawn left threat

        if (this.isWithinTile(LOCATION_X + subtractor, LOCATION_Y - subtractor) && noThreat) {
            noThreat = !board.getTileOnBoard(new Location(LOCATION_X + subtractor, LOCATION_Y - subtractor))
                    .getTileOccupiedByOpponentPawn(this);
        } // check pawn right threat
        return noThreat;
    }

    // used when castling, or moving a king
    // return false if king move to threatening position
    public boolean noThreat(final Board board, final Location newLocation) {
        final Location oriLocation = this.getLocation();
        this.setLocation(newLocation);
        boolean noThreat = true;

        // BISHOP OR QUEEN OR KING THREAT
        noThreat = searchAllDiagonalThreat(board, newLocation);

        // QUEEN OR ROOK OR KING THREAT
        if (noThreat) {
            noThreat = searchAllStraightThreat(board, newLocation);
        }

        // PAWN THREAT
        if (noThreat) {
            noThreat = searchAllPawnThreat(board, newLocation);
        }

        // KNIGHT THREAT
        if (noThreat) {
            noThreat = searchAllKnightThreat(board, newLocation);
        }
        this.setLocation(oriLocation);
        return noThreat;
    }

    public boolean checkKingSafe(final Board board, final Location previousLocation, final Piece piece,
            final boolean isDiscoverCheck) {
        final Location pieceLocation = piece.getLocation();
        final int PIECE_Y = previousLocation.getY(), PIECE_X = previousLocation.getX();
        final int PIECE_NEW_Y = pieceLocation.getY(), PIECE_NEW_X = pieceLocation.getX();
        final int KING_Y = this.getLocation().getY(), KING_X = this.getLocation().getX();

        // if left OR right piece dont move horizontally
        if (KING_Y == PIECE_Y && PIECE_NEW_Y != PIECE_Y) {

            if (PIECE_X > KING_X && board.getTileOnBoard(new Location(KING_X + 1, KING_Y)).getTileNotOccupied()) {// check
                                                                                                                  // right
                                                                                                                  // horizontal
                                                                                                                  // threat
                return searchStraightThreat(board, this.getLocation(), 1, isDiscoverCheck, false);
            }

            else if (KING_X > PIECE_X && board.getTileOnBoard(new Location(KING_X - 1, KING_Y)).getTileNotOccupied()) {// check
                                                                                                                       // left
                                                                                                                       // horizontal
                                                                                                                       // threat
                return searchStraightThreat(board, this.getLocation(), 2, isDiscoverCheck, false);
            }
        }

        // if top OR bottom piece dont move vertically
        else if (KING_X == PIECE_X && PIECE_NEW_X != PIECE_X) {

            if (PIECE_Y > KING_Y && board.getTileOnBoard(new Location(KING_X, KING_Y + 1)).getTileNotOccupied()) {// check
                                                                                                                  // lower
                                                                                                                  // vertical
                                                                                                                  // threat
                return searchStraightThreat(board, this.getLocation(), 3, isDiscoverCheck, false);
            }

            else if (KING_Y > PIECE_Y && board.getTileOnBoard(new Location(KING_X, KING_Y - 1)).getTileNotOccupied()) {// check
                                                                                                                       // upper
                                                                                                                       // vertical
                                                                                                                       // threat
                return searchStraightThreat(board, this.getLocation(), 4, isDiscoverCheck, false);
            }
        }
        // if diagonal piece dont move diagonally
        else if (Math.abs(KING_Y - PIECE_Y) == Math.abs(KING_X - PIECE_X)
                && !(Math.abs(KING_Y - PIECE_NEW_Y) == Math.abs(KING_X - PIECE_NEW_X))) {

            if (KING_Y - PIECE_Y > 0 && KING_X - PIECE_X > 0
                    && board.getTileOnBoard(new Location(KING_X - 1, KING_Y - 1)).getTileNotOccupied()) {// check upper
                                                                                                         // left threat
                return searchDiagonalThreat(board, this.getLocation(), 1, isDiscoverCheck, false);
            }

            else if (KING_Y - PIECE_Y > 0 && KING_X - PIECE_X < 0
                    && board.getTileOnBoard(new Location(KING_X + 1, KING_Y - 1)).getTileNotOccupied()) {// check upper
                                                                                                         // right threat
                return searchDiagonalThreat(board, this.getLocation(), 2, isDiscoverCheck, false);
            }

            else if (KING_Y - PIECE_Y < 0 && KING_X - PIECE_X > 0
                    && board.getTileOnBoard(new Location(KING_X - 1, KING_Y + 1)).getTileNotOccupied()) {// check lower
                                                                                                         // left threat
                return searchDiagonalThreat(board, this.getLocation(), 3, isDiscoverCheck, false);
            }

            else if (KING_Y - PIECE_Y < 0 && KING_X - PIECE_X < 0
                    && board.getTileOnBoard(new Location(KING_X + 1, KING_Y + 1)).getTileNotOccupied()) {// check lower
                                                                                                         // right threat
                return searchDiagonalThreat(board, this.getLocation(), 4, isDiscoverCheck, false);
            }
        }
        return true;
    }

    private boolean searchKnightThreat(final Board board, final Location previousLocation) {
        boolean noThreat = false;
        final int LOCATION_X = previousLocation.getX();
        final int LOCATION_Y = previousLocation.getY();
        if (this.isWithinTile(LOCATION_X, LOCATION_Y)) {
            noThreat = board.getTileOnBoard(previousLocation).getTileOccupiedByOpponentKnight(this);
        }
        return noThreat;
    }

    private boolean searchAllKnightThreat(final Board board, final Location previousLocation) {
        boolean noThreat = false;
        final int LOCATION_X = previousLocation.getX(), LOCATION_Y = previousLocation.getY();
        if (!noThreat) {
            noThreat = searchKnightThreat(board, new Location(LOCATION_X + 2, LOCATION_Y + 1));
        }
        if (!noThreat) {
            noThreat = searchKnightThreat(board, new Location(LOCATION_X + 2, LOCATION_Y - 1));
        }
        if (!noThreat) {
            noThreat = searchKnightThreat(board, new Location(LOCATION_X - 2, LOCATION_Y + 1));
        }
        if (!noThreat) {
            noThreat = searchKnightThreat(board, new Location(LOCATION_X - 2, LOCATION_Y - 1));
        }
        if (!noThreat) {
            noThreat = searchKnightThreat(board, new Location(LOCATION_X + 1, LOCATION_Y - 2));
        }
        if (!noThreat) {
            noThreat = searchKnightThreat(board, new Location(LOCATION_X + 1, LOCATION_Y + 2));
        }
        if (!noThreat) {
            noThreat = searchKnightThreat(board, new Location(LOCATION_X - 1, LOCATION_Y - 2));
        }
        if (!noThreat) {
            noThreat = searchKnightThreat(board, new Location(LOCATION_X - 1, LOCATION_Y + 2));
        }
        return !noThreat;
    }

    private boolean searchStraightThreat(final Board board, final Location previousLocation, final int i,
            final boolean isDiscoverCheck, final boolean isKing) {
        int LOCATION_X = previousLocation.getX(), LOCATION_Y = previousLocation.getY();
        while (this.isWithinTile(LOCATION_X, LOCATION_Y)
                || board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileNotOccupied()) {
            if (i == 1) {
                LOCATION_X++;
            } else if (i == 2) {
                LOCATION_X--;
            } else if (i == 3) {
                LOCATION_Y++;
            } else if (i == 4) {
                LOCATION_Y--;
            }

            if (!(this.isWithinTile(LOCATION_X, LOCATION_Y))) {
                break;
            }
            if (isDiscoverCheck) {
                this.discoverCheckCoordinate.add(new Move(new Location(LOCATION_X, LOCATION_Y)));
            }
            if (isKing) {
                if (board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileOccupiedByOpponentKing(this)) {
                    return false;
                }
                return true;
            } else {
                if (board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileOccupiedByOpponentQueen(this)
                        || board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y))
                                .getTileOccupiedByOpponentRook(this)) {
                    if (isDiscoverCheck) {
                        this.discoverCheckYX = LOCATION_Y * 10 + LOCATION_X;
                    }
                    return false;
                } else if (!board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileNotOccupied()) {
                    if (isDiscoverCheck && !this.discoverCheckCoordinate.isEmpty()) {
                        this.discoverCheckCoordinate.clear();
                    }
                    return true;
                }
            }
        }
        if (isDiscoverCheck && !this.discoverCheckCoordinate.isEmpty()) {
            this.discoverCheckCoordinate.clear();
        }
        return true;
    }

    private boolean straightThreat(final Board board, final Location kingLocation, final int cases,
            final Location previousLocation) {
        boolean noThreat = true;
        final int LOCATION_X = previousLocation.getX();
        final int LOCATION_Y = previousLocation.getY();
        if (this.isWithinTile(LOCATION_X, LOCATION_Y)
                && !board.getTileOnBoard(previousLocation).getTileOccupiedByOwnPiece(this)) {
            noThreat = searchStraightThreat(board, kingLocation, cases, false, true);
            if (noThreat) {
                noThreat = searchStraightThreat(board, kingLocation, cases, false, false);
            }
        } // check straight upper threat
        return noThreat;
    }

    private boolean searchAllStraightThreat(final Board board, final Location previousLocation) {
        boolean noThreat = true;
        if (noThreat) {// check straight upper threat
            noThreat = straightThreat(board, previousLocation, 4, new Location(previousLocation.getX(), previousLocation.getY() - 1));
        }
        if (noThreat) {// check straight lower threat
            noThreat = straightThreat(board, previousLocation, 3, new Location(previousLocation.getX(), previousLocation.getY() + 1));
        }
        if (noThreat) {// check straight left threat
            noThreat = straightThreat(board, previousLocation, 2, new Location(previousLocation.getX() - 1, previousLocation.getY()));
        }
        if (noThreat) {// check straight right threat
            noThreat = straightThreat(board, previousLocation, 1, new Location(previousLocation.getX() + 1, previousLocation.getY()));
        }
        return noThreat;
    }

    private boolean searchDiagonalThreat(final Board board, final Location previousLocation, final int i,
            final boolean isDiscoverCheck, final boolean isKing) {
        int LOCATION_X = previousLocation.getX(), LOCATION_Y = previousLocation.getY();
        while (this.isWithinTile(LOCATION_X, LOCATION_Y)
                || board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileNotOccupied()) {
            if (i == 1) {
                LOCATION_Y--;
                LOCATION_X--;
            } else if (i == 2) {
                LOCATION_Y--;
                LOCATION_X++;
            } else if (i == 3) {
                LOCATION_Y++;
                LOCATION_X--;
            } else if (i == 4) {
                LOCATION_Y++;
                LOCATION_X++;
            }
            if (!(this.isWithinTile(LOCATION_X, LOCATION_Y))) {
                break;
            }
            if (isDiscoverCheck) {
                this.discoverCheckCoordinate.add(new Move(new Location(LOCATION_X, LOCATION_Y)));
            }
            if (isKing) {
                if (board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileOccupiedByOpponentKing(this)) {
                    return false;
                }
                return true;
            } else {
                if (board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileOccupiedByOpponentQueen(this)
                        || board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileOccupiedByOpponentBishop(this)) {
                    if (isDiscoverCheck) {
                        this.discoverCheckYX = LOCATION_Y * 10 + LOCATION_X;
                    }
                    return false;
                } else if (!board.getTileOnBoard(new Location(LOCATION_X, LOCATION_Y)).getTileNotOccupied()) {
                    if (isDiscoverCheck && !this.discoverCheckCoordinate.isEmpty()) {
                        this.discoverCheckCoordinate.clear();
                    }
                    return true;
                }
            }
        }
        if (isDiscoverCheck && !this.discoverCheckCoordinate.isEmpty()) {
            this.discoverCheckCoordinate.clear();
        }
        return true;
    }

    private boolean diagonalThreat(final Board board, final Location kingLocation, final int cases,
            final Location previousLocation) {
        boolean noThreat = true;
        final int LOCATION_X = previousLocation.getX();
        final int LOCATION_Y = previousLocation.getY();
        if (this.isWithinTile(LOCATION_X, LOCATION_Y)
                && !board.getTileOnBoard(previousLocation).getTileOccupiedByOwnPiece(this)) {
            // *CHECK if king is near the board then only other pieces*
            noThreat = searchDiagonalThreat(board, kingLocation, cases, false, true);
            if (noThreat) {
                noThreat = searchDiagonalThreat(board, kingLocation, cases, false, false);
            }
        }
        return noThreat;
    }

    private boolean searchAllDiagonalThreat(final Board board, final Location previousLocation) {
        boolean noThreat = true;
        if (noThreat) {// check upper left threat
            noThreat = diagonalThreat(board, previousLocation, 1, new Location(previousLocation.getX() - 1, previousLocation.getY() - 1));
        }
        if (noThreat) {// check upper right threat
            noThreat = diagonalThreat(board, previousLocation, 2, new Location(previousLocation.getX() + 1, previousLocation.getY() - 1));
        }
        if (noThreat) {// check lower left threat
            noThreat = diagonalThreat(board, previousLocation, 3, new Location(previousLocation.getX() - 1, previousLocation.getY() + 1));
        }
        if (noThreat) {// check lower right threat
            noThreat = diagonalThreat(board, previousLocation, 4, new Location(previousLocation.getX() + 1, previousLocation.getY() + 1));
        }
        return noThreat;
    }

    protected boolean kingInDirectCheck(final Board board, final Piece piece) {
        final Location pieceLOcation = piece.getLocation();
        final int NEW_LOCATION_Y = pieceLOcation.getY(), NEW_LOCATION_X = pieceLOcation.getX();

        final int KING_Y = this.getLocation().getY(), KING_X = this.getLocation().getX();

        final int xDifference = Math.abs(NEW_LOCATION_X - KING_X);
        final int yDifference = Math.abs(NEW_LOCATION_Y - KING_Y);
        boolean check = false;

        // if Queen or Rook might check king horizontally
        if (KING_Y == NEW_LOCATION_Y && KING_X != NEW_LOCATION_X) {
            final boolean isQueen = board.getTileOnBoard(pieceLOcation).getTileOccupiedByOpponentQueen(this);
            final boolean isRook = board.getTileOnBoard(pieceLOcation).getTileOccupiedByOpponentRook(this);
            if (isQueen || isRook) {
                check = findStraightDirectCheck(board, this.getLocation(), pieceLOcation, false);
            }
        }

        // if Queen or Rook might check king vetically
        if (KING_X == NEW_LOCATION_X && KING_Y != NEW_LOCATION_Y) {
            final boolean isQueen = board.getTileOnBoard(pieceLOcation).getTileOccupiedByOpponentQueen(this);
            final boolean isRook = board.getTileOnBoard(pieceLOcation).getTileOccupiedByOpponentRook(this);
            if (isQueen || isRook)
                check = findStraightDirectCheck(board, this.getLocation(), pieceLOcation, true);
        }

        // if Queen, Bishop, might check king diagonally
        if (xDifference == yDifference) {
            final boolean isBishop = board.getTileOnBoard(pieceLOcation).getTileOccupiedByOpponentBishop(this);
            final boolean isQueen = board.getTileOnBoard(pieceLOcation).getTileOccupiedByOpponentQueen(this);
            if (isBishop || isQueen) {
                boolean topLeftBottomRight = false;
                if ((NEW_LOCATION_X > KING_X && NEW_LOCATION_Y > KING_Y)
                        || (NEW_LOCATION_X < KING_X && NEW_LOCATION_Y < KING_Y))
                    topLeftBottomRight = true;
                check = findDiagonalDirectCheck(board, this.getLocation(), pieceLOcation, topLeftBottomRight);
            }
        }
        // if Knight can check King
        if ((xDifference == 1 && yDifference == 2 || xDifference == 2 && yDifference == 1)) {
            final boolean isKnight = board.getTileOnBoard(pieceLOcation).getTileOccupiedByOpponentKnight(this);
            if (isKnight) {
                this.directCheckCoordinate.add(new Move(pieceLOcation));
                check = true;
            }
        }

        // pawn check king
        // as long as there is pawn in front of king at left or right
        if (xDifference == 1 && yDifference == 1 && (KING_Y - this.getOFFSET()) == NEW_LOCATION_Y) {
            final boolean isPawn = board.getTileOnBoard(pieceLOcation).getTileOccupiedByOpponentPawn(this);
            if (isPawn) {
                this.directCheckCoordinate.add(new Move(pieceLOcation));
                check = true;
            }
        }
        this.kingInDirectCheck = check;
        if (!this.kingInDirectCheck && !this.directCheckCoordinate.isEmpty()) {
            this.directCheckCoordinate.clear();
        }
        return check;
    }

    private boolean findDiagonalDirectCheck(final Board board, final Location kingLocation, final Location location,
            final boolean topLeftBottomRight) {

        final int KING_X = kingLocation.getX(), KING_Y = kingLocation.getY();
        final int LOCATION_X = location.getX(), LOCATION_Y = location.getY();
        final int biggestX = Math.max(LOCATION_X, KING_X), biggestY = Math.max(LOCATION_Y, KING_Y);
        final int smallestX = Math.min(LOCATION_X, KING_X);
        int y = biggestY - 1, x = biggestX - 1, counter = smallestX;

        if (!topLeftBottomRight) {
            x = smallestX + 1;
            counter = biggestX;
        }
        while (x != counter && this.isWithinTile(x, y)) {
            if (!board.getTileOnBoard(new Location(x, y)).getTileNotOccupied()) {
                this.directCheckCoordinate.clear();
                return false;
            }
            final Location checkLocation = new Location(x, y);
            this.directCheckCoordinate.add(new Move(checkLocation));
            if (topLeftBottomRight)
                x--;
            else
                x++;
            y--;
        }
        this.directCheckCoordinate.add(new Move(location));
        return board.getTileOnBoard(location).getTileOccupiedByOpponent(this);
    }

    private boolean findStraightDirectCheck(final Board board, final Location kingLocation, final Location location,
            final boolean isVertical) {
        final int LOCATION_X = location.getX(), LOCATION_Y = location.getY();
        final int KING_X = kingLocation.getX(), KING_Y = kingLocation.getY();
        int biggest = 0, smallest = 0;

        if (isVertical) {
            biggest = Math.max(LOCATION_Y, KING_Y);
            smallest = Math.min(LOCATION_Y, KING_Y);
        } else {
            biggest = Math.max(LOCATION_X, KING_X);
            smallest = Math.min(LOCATION_X, KING_X);
        }

        biggest--;
        while (biggest != smallest && biggest >= 0 && biggest <= 7) {
            if (isVertical) {
                if (!board.getTileOnBoard(new Location(LOCATION_X, biggest)).getTileNotOccupied()) {
                    this.directCheckCoordinate.clear();
                    return false;
                }
                final Location checkLocation = new Location(LOCATION_X, biggest);
                this.directCheckCoordinate.add(new Move(checkLocation));
            } else {
                if (!board.getTileOnBoard(new Location(biggest, LOCATION_Y)).getTileNotOccupied()) {
                    this.directCheckCoordinate.clear();
                    return false;
                }
                final Location checkLocation = new Location(biggest, LOCATION_Y);
                this.directCheckCoordinate.add(new Move(checkLocation));
            }
            biggest--;
        }
        this.directCheckCoordinate.add(new Move(location));
        return board.getTileOnBoard(location).getTileOccupiedByOpponent(this);
    }

    public void kingInCheck(final Board board, final Piece piece, final Location oriLocation) {
        // For finding any direct check
        this.kingInDirectCheck = this.kingInDirectCheck(board, piece);
        // For finding any discover check
        // the logic for finding discover check is the same as
        // finding if moving a piece will expose the king or not
        this.kingInDiscoveredCheck = !this.checkKingSafe(board, oriLocation, piece, true);
        if (!this.kingInDiscoveredCheck) {
            this.discoverCheckYX = -1;
        }

        // For finding any double check
        this.kingInDoubleCheck = this.kingInDirectCheck && this.kingInDiscoveredCheck;
        if (this.kingInDoubleCheck) {// Since only a king can move in double check
            Piece.onlyKingCanMove(board.getCurrentPlayer().getOpponent().getAllMyPieces(), true);
        }
    }
}