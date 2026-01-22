package chess.MoveCalculations;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class BishopMoves extends OverallMoves {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[] row = {-1, -1, 1, 1};
        int[] col = {-1, 1, -1, 1};
        BoardEdge(board, myPosition, row, col);
    }
}
