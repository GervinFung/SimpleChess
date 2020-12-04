
package chess.pieces.piece_move;

import chess.boardUtils.Board;
import chess.pieces.King;

public interface FiniteMove{
    public abstract Move calculateLegalMoves(final Board board, final int i, final King king);
}