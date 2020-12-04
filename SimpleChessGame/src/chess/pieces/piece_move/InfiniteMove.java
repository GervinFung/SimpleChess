package chess.pieces.piece_move;

import java.util.ArrayList;

import chess.boardUtils.Board;
import chess.pieces.King;

public interface InfiniteMove{
    public abstract ArrayList<Move> calculateLegalMoves(final Board board, final int i, final King king);
}
